package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


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
	 * A list of pages currently on this wiki.
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
	 * A list of changes recently made to this wiki.
	 */
	fun recentChanges() : WikiResult<List<WikiRecentChange>>? {
		val response = if(!settings.offline) api.makeGetRequest("recent-changes", mapOf<String, String>(
			"format" to "json"
		)) else null
		var source = Source.Internet
		val data: String = if(response !== null) {
			// Download successful, cache it
			dataManager.cacheString("recentchanges", "$name.json", response.body)
			response.body
		}
		else {
			// Fetch from Internet failed, try the cache
			source = Source.Cache
			dataManager.getCachedString("recentchanges", "$name.json") ?: return null
		}
		
		return WikiResult(source, parse_recent_changes(data))
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
	
	/**
	 * Returns the page's markdown (i.e. the raw source of the page).
	 * CAUTION: ALWAYS fetches from the Internet - there's no cache here!
	 * @param pagename: The name of the page to fetch the source for.
	 * @return The raw source of the specified page.
	 */
	fun pageSource(pagename: String) : WikiResult<String>? {
		Log.i("Wiki", "Fetching page source for $pagename")
		val response : WikiApiResponse? = api.makeGetRequest("raw", mapOf(
			"page" to pagename
		))
		return if(response == null) null
		else WikiResult(Source.Internet, response.body)
	}
	
	/**
	 * Searches the wiki for a query query string.
	 * Returns an ordered list of WikiSearchResult objects.
	 * Important: An Internet connection is *required* for this method to work!
	 * Any search results we cache are not guaranteed to be up to date, and it's also questionable
	 * as to whether a user will search for the same thing twice. If I'm wrong here and you do
	 * search for the same thing more than once and caching searches would in fact be useful,
	 * please open an issue. 
	 * @param	query: The query string to search for.
	 * @return	An ordered list of WikiSearchResult objects.
	 */
	fun search(query: String) : WikiResult<List<WikiSearchResult>>? {
		Log.i("Wiki", "Searching for '$query'")
		
		if(settings.offline) return null
		
		val response : WikiApiResponse = api.makeGetRequest("search", mapOf(
			"format" to "json",
			"query" to query
		)) ?: return null
		
		return try {
			WikiResult(Source.Internet, parse_search_results(response.body))
		} catch (error: JSONException) {
			Log.w("Wiki", "Warning: Caught error while parsing JSON: '${error.message}")
			null
		}
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