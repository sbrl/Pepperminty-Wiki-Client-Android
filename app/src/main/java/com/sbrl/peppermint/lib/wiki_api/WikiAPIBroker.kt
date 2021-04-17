package com.sbrl.peppermint.lib.wiki_api

import android.annotation.SuppressLint
import android.util.Log
import com.sbrl.peppermint.lib.net.MemoryCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.net.URLEncoder

class WikiAPIBroker (inEndpoint: String, inCredentials: WikiCredentials?) {
	private val client: OkHttpClient = OkHttpClient.Builder()
		.followRedirects(false)    // Redirects are a valuable source of info from Pepperminty Wiki
		.cookieJar(MemoryCookieJar())           // Without this okhttp wouldn't save any cookies
		.build()
	
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
		)) ?: return false
		return response.hasHeader("x-login-success")
	}
	
	private fun sendRequest(request: Request, isLogin: Boolean = false) : WikiApiResponse? {
		Log.i("WikiAPIBroker:sendRequest", "${request.method} ${request.url}")
		val response = try {
			client.newCall(request).execute()
		}
		catch (error: IOException) {
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
	
	fun makePostRequest(action: String, propertiesGet: Map<String, String>?, propertiesPost: Map<String, String>?) : WikiApiResponse? {
		val url = makeUrl(action, propertiesGet)
		
		return sendRequest(Request.Builder()
			.url(url)
			.post(postify(propertiesPost ?: mapOf())
				.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
			.build())
	}
	
	fun makeGetRequest(action: String, properties: Map<String, String>?) : WikiApiResponse? {
		val url = makeUrl(action, properties)
		
		return sendRequest(Request.Builder()
			.url(url)
			.build())
	}
	fun makeGetRequest(action: String) : WikiApiResponse? {
		return makeGetRequest(action, mapOf())
	}
	
	fun testConnection(): ConnectionStatus {
		val response = makeGetRequest("status", HashMap())
			?: return ConnectionStatus.ConnectionFailed
		
		if(response.isLoginRequired())
			return ConnectionStatus.CredentialsRequired
		
		if(response.statusCode < 200 || response.statusCode >= 300)
			return ConnectionStatus.ConnectionFailed
		
		return ConnectionStatus.Ok
	}
}