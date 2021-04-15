package com.sbrl.peppermint.lib

import com.sbrl.peppermint.lib.helpers.HashMapBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.net.URL
import java.net.URLEncoder
import kotlin.text.StringBuilder

class WikiAPIBroker (inEndpoint: String, inCredentials: WikiCredentials?) {
	private var client: OkHttpClient = OkHttpClient()
	
	private var url: String = inEndpoint
		get() = field
		set(value) {
			field = value
			connectionStatus = ConnectionStatus.Untested
		}
	
	private var credentials: WikiCredentials? = inCredentials
		set(value) {
			field = value
			connectionStatus = ConnectionStatus.Untested
		}
	
	var connectionStatus: ConnectionStatus = ConnectionStatus.Untested
	
	private fun urlencode(str: String) : String {
		return URLEncoder.encode(str, "utf-8")
	}
	
	private fun makeUrl(action: String, properties: HashMap<String, String>?) : String {
		val url = StringBuilder()
		url.append(url)
		url.append("?action=" + urlencode(action))
		if(properties !== null) {
			for ((key, value) in properties)
				url.append("${urlencode(key)}=${urlencode(value)}")
		}
		return url.toString()
	}
	
	fun makeGetRequest(action: String, properties: HashMap<String, String>?) : WikiApiResponse? {
		val url = makeUrl(action, properties)
		
		val request = Request.Builder()
			.url(url)
			.build()
		
		val response = try {
			client.newCall(request).execute()
		}
		catch (error: IOException) {
			return null
		}
		val result = WikiApiResponse(response)
		
		response.close()
		return result
	}
	fun makeGetRequest(action: String) : WikiApiResponse? {
		return makeGetRequest(action, HashMap<String, String>())
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