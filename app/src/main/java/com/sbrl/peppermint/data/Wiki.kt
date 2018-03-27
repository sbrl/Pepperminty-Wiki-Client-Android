package com.sbrl.peppermint.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.sbrl.peppermint.bricks.DataStorer
import com.sbrl.peppermint.bricks.TextDownloader
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
	
	private val cacheIdPageList get() = "$Name-page-list.txt"
	
	public val Name : String
	public val Info : WikiCredentials
	
	private val context : Context
	private val storage : DataStorer
	private val downloader = TextDownloader()
	
	constructor(inContext : Context, inName : String, wikiInfo : WikiCredentials) {
		Name = inName
		Info = wikiInfo
		
		context = inContext
		storage = DataStorer(context)
		// TODO: Login here if credentials are provided
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
				)
			)
		} catch(error : ConnectException) {
			Log.w(LogTag, error.message)
			error.printStackTrace()
			return ConnectionStatus.ConnectionFailed
		}
		if(status.statusCode < 200 || status.statusCode > 299)
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
		val params = mapOf(
			"action" to "list",
			"format" to "text"
		)
		val rawList = khttp.get(Info.RootUrl, params = params)
		Log.i(LogTag, "Downloaded ${rawList.text.length} byte page list.")
		
		storage.CacheString(cacheIdPageList, rawList.text)
		
		return rawList.text
	}
}