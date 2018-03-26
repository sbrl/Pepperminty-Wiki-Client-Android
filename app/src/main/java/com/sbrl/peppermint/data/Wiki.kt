package com.sbrl.peppermint.data

import android.content.Context
import android.util.Log
import com.sbrl.peppermint.bricks.DataStorer
import com.sbrl.peppermint.bricks.TextDownloader

class Wiki {
	private val LogTag = "Wiki"
	
	private val cacheIdPageList get() = "$Name-page-list.txt"
	
	public val Name : String
	public val Info : WikiCredentials
	
	private val storage : DataStorer
	private val downloader = TextDownloader()
	
	constructor(context : Context, inName : String, wikiInfo : WikiCredentials) {
		Name = inName
		Info = wikiInfo
		
		storage = DataStorer(context)
		// TODO: Login here if credentials are provided
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
		val rawList = khttp.get(Info.RootUrl, params)
		
		Log.v(LogTag, "Downloaded ${rawList.text.length} byte page list.")
		
		storage.CacheString(cacheIdPageList, rawList.text)
		
		return rawList.text
	}
}