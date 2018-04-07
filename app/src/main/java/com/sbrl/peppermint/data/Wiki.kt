package com.sbrl.peppermint.data

import android.content.Context
import android.util.Log
import com.sbrl.peppermint.bricks.DataManager
import khttp.responses.Response
import org.json.JSONException
import org.json.JSONObject
import java.net.ConnectException

public enum class ConnectionStatus {
	// The connection is ok
	Ok,
	// We couldn't connect (connection refused or similar)
	ConnectionFailed,
	// We didn't get a 2xx response
	WrongHttpCode,
	// The status returned wasn't valid JSON
	InvalidStatus,
	// The status was returned correctly, but indicates that the wiki isn't ok
	UnhealthyStatus
}

class Wiki {
	private val LogTag = "Wiki"
	
	public val LoginCookieName = "PHPSESSID"
	
	private val cacheIdPageList get() = "$Name-page-list.txt"
	private fun cacheIdPageHtml(pageName: String) : String = "$Name/pages/$pageName"
	
	public val Name : String
	public val Info : WikiCredentials
	
	private val context : Context
	private val storage : DataManager
	private val prefs : PreferencesManager
	
	constructor(inContext : Context, inName : String, wikiInfo : WikiCredentials, testAndLogin : Boolean = true) {
		Name = inName
		Info = wikiInfo
		
		context = inContext
		storage = DataManager(context)
		prefs = PreferencesManager(context)
		
		if(testAndLogin)
			TestConnection() // Test the connection - logging in if required
	}
	
	public fun TestConnection(doLogin: Boolean = true) : ConnectionStatus {
		lateinit var status : Response
		try {
			status = khttp.request(
				method = "GET",
				url = Info.RootUrl,
				params = mapOf(
					"action" to "status",
					"minified" to "true"
				),
				headers = mapOf(
					"Accept" to "application/json"
				),
				cookies = mapOf( LoginCookieName to prefs.GetSessionToken() ),
				allowRedirects = false
			)
			Log.i(LogTag, "Status code: ${status.statusCode}")
			Log.i(LogTag, "Sent accept header: " + status.request.headers["Accept"])
			if(responseRequiresLogin(status) && doLogin) {
				Log.i(LogTag, "TestConnection: Attempting login")
				login() // Login
				return TestConnection(false)
			}
		} catch(error : ConnectException) {
			Log.w(LogTag, error.message)
			error.printStackTrace()
			return ConnectionStatus.ConnectionFailed
		}
		
		if(status.statusCode !in 200..299)
			return ConnectionStatus.WrongHttpCode
		
		try {
			val statusData = JSONObject(status.text)
			val wikiStatus = statusData.getString("status")
			if(wikiStatus != "ok") {
				Log.w(LogTag, "Unhealthy status $wikiStatus returned by $Name")
				return ConnectionStatus.UnhealthyStatus
			}
			Log.i(LogTag, "Wiki connection test ok")
			return ConnectionStatus.Ok
		} catch(error : JSONException) {
			Log.w(LogTag, error.message)
			error.printStackTrace()
			return ConnectionStatus.InvalidStatus
		}
	}
	
	public fun GetPageList(refreshFromInternet : Boolean) : List<String> {
		var rawPageList : String? = if(refreshFromInternet)
			downloadPageList()
		else
			storage.GetCachedString(cacheIdPageList)
		if(rawPageList == null)
			rawPageList = downloadPageList()
		
		// Parse out the page list
		val result = ArrayList<String>()
		for(nextPage in rawPageList.lines())
			result.add(nextPage.trim())
		return result
	}
	
	private fun downloadPageList() : String {
		val rawList = khttp.get(
			Info.RootUrl,
			params = mapOf(
				"action" to "list",
				"format" to "text"
			),
			cookies = mapOf( LoginCookieName to prefs.GetSessionToken() )
		)
		Log.i(LogTag, "Downloaded ${rawList.text.length} byte page list.")
		
		storage.CacheString(cacheIdPageList, rawList.text)
		
		return rawList.text
	}
	
	public fun GetPageHTML(pageName: String, refreshFromInternet: Boolean) : String {
		return if(refreshFromInternet || !storage.HasCachedData(cacheIdPageHtml(pageName)))
			downloadPageHTML(pageName)
		else
			storage.GetCachedString(cacheIdPageHtml(pageName))!!
	}
	
	
	private fun downloadPageHTML(pageName : String) : String {
		val response = khttp.get(
			Info.RootUrl,
			params = mapOf(
				"action" to "view",
				"mode" to "parsedsourceonly",
				"page" to pageName
			),
			allowRedirects = false,
			cookies = mapOf( LoginCookieName to prefs.GetSessionToken() )
		)
		
		storage.CacheString(cacheIdPageHtml(pageName), response.text)
		
		return response.text
	}
	
	private fun responseRequiresLogin(response : Response) : Boolean {
		return response.headers.contains("x-login-required")
	}
	
	private fun login() : Boolean {
		val response = khttp.post(
			Info.RootUrl,
			params = mapOf(
				"action" to "checklogin"
			),
			headers = mapOf(
				"accept" to "application/json"
			),
			data = mapOf(
				"user" to Info.Username,
				"pass" to Info.Password
			),
			allowRedirects = false
		)
		Log.i(LogTag, "Login status code: ${response.statusCode}")
		Log.i(LogTag, response.text)
		Log.i(LogTag, "Login successful: ${response.headers["x-login-success"]}")
		if(response.headers["x-login-success"] == "yes") {
			prefs.SaveSessionToken(response.cookies[LoginCookieName]!!)
			return true
		}
		
		return false
	}
}