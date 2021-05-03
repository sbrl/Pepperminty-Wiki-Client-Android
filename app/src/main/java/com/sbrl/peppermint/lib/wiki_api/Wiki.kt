package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager
import org.json.JSONObject



class Wiki(
	private val settings: SettingsManager,
	private val dataManager: DataManager,
	val id: String,
	val name: String,
	inEndpoint: String,
	inCredentials: WikiCredentials?) {
	constructor(settings: SettingsManager, inDataManager: DataManager, inId: String, inName: String, inEndpoint: String)
		: this(settings, inDataManager, inId, inName, inEndpoint, null)
	
	var api: WikiAPIBroker = WikiAPIBroker(inEndpoint, inCredentials)
	
	enum class Source { Internet, Cache }
	
	data class WikiResult<T>(val source: Source, val value: T)
	
	fun connectionStatus(): ConnectionStatus {
		if(api.connectionStatus == ConnectionStatus.Untested)
			api.connectionStatus = api.testConnection()
		
		return api.connectionStatus
	}
	
	/**
	 * Pages a list of pages currently on this wiki.
	 * @return A list of pages as a list of strings.
	 */
	fun pages(): WikiResult<List<String>>? {
		val response = if(!settings.offline) api.makeGetRequest("list", mapOf<String, String>(
			"format" to "text"
		)) else null
		var source = Source.Internet
		val data: String = if(response !== null) {
				// Cache the newly downloaded string
				dataManager.cacheString("pagelists", "$name.json", response.body)
				response.body
			}
			else {
				// Failed to fetch from the Internet, try the cache
				source = Source.Cache
				dataManager.getCachedString("pagelists", "$name.json") ?: return null
			}
		
		return WikiResult(source, data.lines())
	}
	
	/**
	 * Fetches the content of a specific page.
	 * @param pagename: The name of the page to fetch the content for.
	 * @param format: The format to retrieve the content in.
	 * @return The content of the page in the specified format.
	 */
	fun pageContent(pagename: String, format: PageContentType = PageContentType.HTML) : WikiResult<String>? {
		Log.i("Wiki", "Fetching page content for $pagename")
		
		val ext = when(format) { PageContentType.HTML -> "html" }
		
		val response : WikiApiResponse? = if(!settings.offline) api.makeGetRequest("view", mapOf(
			"page" to pagename,
			"mode" to "parsedsourceonly"
		)) else null
		var source = Source.Internet
		val data: String = if(response !== null) {
				// Update the cache
				dataManager.cacheString("wiki:$name", "$pagename.$ext", response.body)
				response.body
			} else {
				// Oops that didn't go too well. Let's see if we can pull from the cache
				source = Source.Cache
				dataManager.getCachedString("wiki:$name", "$pagename.$ext") ?: return null
			}
		return WikiResult(source, data)
	}
	
	// --------------------------------------------------------------------------------------------
	
	fun save() : JSONObject {
		val result = JSONObject()
		result.put("name", name)
		result.put("endpoint", api.endpoint)
		if(api.credentials !== null) {
			result.put("username", api.credentials!!.username)
			result.put("password", api.credentials!!.password)
		}
		return result
	}
	
	companion object {
		fun load(dataManager: DataManager, settings: SettingsManager, id: String, obj: JSONObject) : Wiki {
			// If any credentials are present, extract them
			val credentials = if(obj.has("username") && obj.has("password"))
				WikiCredentials(obj.getString("username"), obj.getString("password"))
			else null
			
			return Wiki(
				settings, dataManager,
				id,
				obj.getString("name"),
				obj.getString("endpoint"),
				credentials
			)
		}
	}
}