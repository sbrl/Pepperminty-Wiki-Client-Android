package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import com.sbrl.peppermint.lib.net.MemoryCookieJar
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder

class WikiAPIBroker (inEndpoint: String, inCredentials: WikiCredentials?) {
	private val client: OkHttpClient = OkHttpClient.Builder()
		.followRedirects(false)    // Redirects are a valuable source of info from Pepperminty Wiki
		.cookieJar(MemoryCookieJar)           // Without this okhttp wouldn't save any cookies
		.build()
	
	/**
	 * The remote version string of the wiki.
	 */
	var versionRemote: WikiVersion? = null
	
	var endpoint: String = inEndpoint
		get() = field
		set(value) {
			field = value
			connectionStatus = ConnectionStatus.Untested
		}
	
	var credentials: WikiCredentials? = inCredentials
		set(value) {
			field = value
			connectionStatus = ConnectionStatus.Untested
		}
	
	var connectionStatus: ConnectionStatus = ConnectionStatus.Untested
	
	/**
	 * Fetches a list of cookies currently in the cookie jar for the wiki HTTP API endpoint.
	 * Very useful if one needs to transfer the cookies, say, into a GeckoView / WebView.
	 */
	fun getCookies() : List<Cookie> {
		return client.cookieJar.loadForRequest(endpoint.toHttpUrlOrNull()!!)
	}
	
	/**
	 * Like Javascript's encodeURIComponent function.
	 */
	private fun urlencode(str: String) : String {
		return URLEncoder.encode(str, "utf-8")
	}
	
	/**
	 * Converts the given map of key-value pairs into a URL encoded string.
	 * Useful for encoding GET parameters or URL-encoded POST data.
	 * @param data: The key-value pairs to encode.
	 */
	private fun postify(data: Map<String, String>) : String {
		val items = mutableListOf<String>()
		for ((key, value) in data)
			items.add("${urlencode(key)}=${urlencode(value)}")
		return items.joinToString("&")
	}
	
	/**
	 * Makes a full absolute URL given an action and optionally a set of GET parameters.
	 * @param action: The action to call against the Pepperminty Wiki API.
	 * @param properties: The GET parameters to encode into the URL.
	 * @return The constructed URL.
	 */
	private fun makeUrl(action: String, properties: Map<String, String>?) : String {
		val url = StringBuilder()
		url.append(endpoint)
		url.append("?action=" + urlencode(action))
		if(properties !== null) {
			url.append("&")
			url.append(postify(properties))
		}
		
		return url.toString()
	}
	
	private fun doLogin(): Boolean {
		Log.i("WikiAPIBroker", "Performing login")
		if(credentials == null) return false // Wat? This should never happen
		val response = makePostRequest("checklogin", null, mapOf(
			"user" to credentials!!.username,
			"pass" to credentials!!.password
		), true) ?: return false
		return response.hasHeader("x-login-success")
	}
	
	private fun sendRequest(request: Request, isLogin: Boolean = false) : WikiApiResponse? {
		Log.i("WikiAPIBroker:sendRequest", "${request.method} ${request.url}")
		val response = try {
			client.newCall(request).execute()
		}
		catch (error: IOException) {
			Log.w("WikiAPIBroker:sendRequest", "Caught network error: $error")
			return null
		}
		val result = WikiApiResponse(response)
		response.close()
		
		// Automatically login if necessary, but only if this isn't already a login request
		if(result.isLoginRequired() && !isLogin) {
			doLogin()
			// Try sending the request again, but don't endlessly try to login
			return sendRequest(request, true)
		}
		
		return result
	}
	
	/**
	 * Makes a POST request against the remote wiki.
	 * @param action: The action in the HTTP API of the remote wiki to call.
	 * @param propertiesGet: The key - value GET parameters to encode in the request.
	 * @param propertiesPost: The key - value POST parameters to encode in the request.
	 * @param isLogin: Whether this request is a login request. Login requests return the raw request request result instead of automatically logging in if required.
	 */
	fun makePostRequest(action: String, propertiesGet: Map<String, String>?, propertiesPost: Map<String, String>?, isLogin: Boolean = false) : WikiApiResponse? {
		val url = makeUrl(action, propertiesGet)
		
		return sendRequest(Request.Builder()
			.url(url)
			.post(postify(propertiesPost ?: mapOf())
				.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
			.build(), isLogin)
	}
	
	/**
	 * Makes a GET request against the remote wiki.
	 * @param action: The action in the HTTP API of the remote wiki to call.
	 * @param properties: The key - value GET parameters to encode in the request.
	 * @param isLogin: Whether this request is a login request. Login requests return the raw request request result instead of automatically logging in if required.
	 */
	fun makeGetRequest(action: String, properties: Map<String, String>?, isLogin: Boolean = false) : WikiApiResponse? {
		val url = makeUrl(action, properties)
		
		return sendRequest(Request.Builder()
			.url(url)
			.build(), isLogin)
	}
	
	fun testConnection(): ConnectionStatus {
		val response = makeGetRequest("status", HashMap())
			?: return ConnectionStatus.ConnectionFailed
		
		if(response.isLoginRequired())
			return ConnectionStatus.CredentialsRequired
		
		if(response.statusCode < 200 || response.statusCode >= 300) {
			if(response.hasHeader("x-login-success")
				&& response.headers["x-login-success"] !== "yes")
					return ConnectionStatus.CredentialsIncorrect
			return ConnectionStatus.ConnectionFailed
		}
		
		versionRemote = extractServerVersion(response.body)
		
		return ConnectionStatus.Ok
	}
	
	private fun extractServerVersion(statusText: String) : WikiVersion? {
		return try {
			val statusInfo = JSONObject(statusText)
			if(statusInfo.has("version"))
				WikiVersion(statusInfo.getString("version"))
			else
				null
		} catch(error : JSONException) {
			Log.w("WikiApiBroker", "Failed to parse JSON or version number from status request.")
			null
		}
	}
}