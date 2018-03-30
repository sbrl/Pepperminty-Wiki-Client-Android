package com.sbrl.peppermint.data

import android.content.Context
import android.util.Log
import com.sbrl.peppermint.bricks.DataStorer
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
	
	private val loginCookieName = "PHPSESSID"
	
	private val cacheIdPageList get() = "$Name-page-list.txt"
	private fun cacheIdPageHtml(pageName: String) : String = "$Name/pages/$pageName"
	
	public val Name : String
	public val Info : WikiCredentials
	
	private val context : Context
	private val storage : DataStorer
	private val prefs : PreferencesManager
	
	constructor(inContext : Context, inName : String, wikiInfo : WikiCredentials) {
		Name = inName
		Info = wikiInfo
		
		context = inContext
		storage = DataStorer(context)
		prefs = PreferencesManager(context)
		
		TestConnection() // Test the connection - logging in if required
	}
	
	public fun TestConnection() : ConnectionStatus {
		lateinit var status : Response
		try {
			status = khttp.get(
				Info.RootUrl,
				headers = mapOf(
					"accept" to "application/json"
				),
				params = mapOf(
					"action" to "status",
					"minified" to "true"
				),
				allowRedirects = false,
				cookies = mapOf( loginCookieName to prefs.GetSessionToken() )
			)
			
			if(responseRequiresLogin(status))
				login()
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
			cookies = mapOf( loginCookieName to prefs.GetSessionToken() )
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
			cookies = mapOf( loginCookieName to prefs.GetSessionToken() )
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
			data = mapOf(
				"user" to Info.Username,
				"password" to Info.Password
			)
		)
		if(response.headers["x-login-success"] == "yes") {
			prefs.SaveSessionToken(response.cookies[loginCookieName]!!)
			return true
		}
		
		return false
	}
}