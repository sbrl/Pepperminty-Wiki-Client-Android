package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import org.json.JSONObject

class Wiki(val id: String,
           val name: String,
           inEndpoint: String,
           inCredentials: WikiCredentials?) {
	constructor(inId: String, inName: String, inEndpoint: String) : this(inId, inName, inEndpoint, null)
	
	var api: WikiAPIBroker = WikiAPIBroker(inEndpoint, inCredentials)
	
	
	fun connectionOk(): ConnectionStatus {
		if(api.connectionStatus != ConnectionStatus.Untested)
			api.connectionStatus = api.testConnection()
		
		return api.connectionStatus
	}
	
	/**
	 * Pages a list of pages currently on this wiki.
	 * @return A lsit of pages as a list of strings.
	 */
	fun pages(): List<String>? {
		val response = api.makeGetRequest("list", mapOf<String, String>(
			"format" to "text"
		)) ?: return null
		Log.i("Wiki", "Page list: '${response.body.substring(0, 100)}' [truncated to 100 chars]")
		return response.body.lines()
	}
	
	/**
	 * Fetches the content of a specific page.
	 * @param pagename: The name of the page to fetch the content for.
	 * @param format: The format to retrieve the content in.
	 * @return The content of the page in the specified format.
	 */
	fun pageContent(pagename: String, format: PageContentType = PageContentType.HTML) : String? {
		Log.i("Wiki", "Fetching page content for $pagename")
		val response = api.makeGetRequest("view", mapOf(
			"page" to pagename,
			"mode" to "parsedsourceonly"
		)) ?: return null
		return response.body
	}
	
	// --------------------------------------------------------------------------------------------
	
	fun save() : JSONObject {
		val result = JSONObject()
		result.put("name", name)
		result.put("endpoint", api.endpoint)
		if(api.credentials !== null) {
			result.put("username", api.credentials!!.username)
			result.put("username", api.credentials!!.password)
		}
		return result
	}
	
	companion object {
		fun load(id: String, obj: JSONObject) : Wiki {
			// If any credentials are present, extract them
			val credentials = if(obj.has("username") && obj.has("password"))
				WikiCredentials(obj.getString("username"), obj.getString("password"))
			else null
			
			return Wiki(
				id,
				obj.getString("name"),
				obj.getString("endpoint"),
				credentials
			)
		}
	}
}