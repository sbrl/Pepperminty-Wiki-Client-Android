package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager
import okhttp3.Cookie
import org.json.JSONException
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
	
	private var api: WikiAPIBroker = WikiAPIBroker(inEndpoint, inCredentials)
	
	val endpoint: String
		get() = api.endpoint
	
	enum class Source { Internet, Cache }
	enum class SaveResult {
		Success, NetworkError, ServerError,
		PermissionsError, PageProtectedError, NoEditsAllowedError,
		ConflictError,
		UnknownClientError, UnknownError
	}
	
	fun cookies() : List<Cookie> {
		return api.getCookies()
	}
	
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
		val response = if(!settings.offline) api.makeGetRequest("list", mapOf(
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
	 * Returns a list of tags on the wiki.
	 * Caches the result locally, but only uses the cache if we are unable to fetch a fresh copy.
	 */
	fun tags() : WikiResult<List<String>?> {
		val response = if(!settings.offline) api.makeGetRequest("list-tags", mapOf(
			"format" to "text"
		)) else null
		
		return if(response == null) {
			val tagListCache = dataManager.getCachedString("listtags", "$name.txt")
			if(tagListCache == null)
				WikiResult.CacheError(WikiError.NetworkErrorAndNoCache)
			else
				WikiResult(Source.Cache, tagListCache.split("""\n""".toRegex()))
		}
		else {
			WikiResult(Source.Internet, response.body.split("""\n""".toRegex()))
		}
	}
	
	/**
	 * A list of changes recently made to this wiki.
	 */
	fun recentChanges() : WikiResult<List<WikiRecentChange>>? {
		val response = if(!settings.offline) api.makeGetRequest("recent-changes", mapOf(
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
	fun pageSource(pagename: String) : WikiResult<WikiPage?> {
		Log.i("Wiki", "Fetching page source for $pagename")
		val response : WikiApiResponse = api.makeGetRequest("raw", mapOf(
			"page" to pagename
		)) ?: return WikiResult.Error(WikiError.NetworkError)
		
		if(!response.hasHeader("x-tags")) {
			Log.w("Wiki", "pageSource: No x-tags header present - the server is probably too old")
			return WikiResult.Error(
				WikiError.OutdatedServer,
				requiredVersion = WikiVersion("0.24")
			)
		}
		
		return WikiResult(Source.Internet, WikiPage(
			response.body,
			response.headers["x-tags"]!!.split(""",\s+""".toRegex())
		))
	}
	
	/**
	 * Acquires an edit key for the given page name.
	 * Call this as *soon* as you fetch the raw source for a page!
	 * This edit eky si a hash of the content of the page. This hash *must* be returned to the server on save in order to detect edit conflicts! 
	 * @param	pagename: The name of the page to fetch an edit key for.
	 * @return	The edit key as a string.
	 */
	fun editKey(pagename: String) : WikiResult<String>? {
		Log.i("Wiki", "Obtaining edit key for $pagename")
		val response : WikiApiResponse = api.makeGetRequest("acquire-edit-key", mapOf(
			"page" to pagename,
			"format" to "json"
		)) ?: return null
		
		return try {
			val response_obj = JSONObject(response.body)
			WikiResult(Source.Internet, response_obj.getString("key"))
		} catch (error: JSONException) {
			Log.w("Wiki", "Error parsing JSON when acquiring edit key!")
			null
		}
	}
	
	/**
	 * Save a page's content back to the remote wiki.
	 * @param pagename: The page name to save content back to.
	 * @param editKey: The edit key obtained BEFORE editing begun from the editKey() function.
	 * @param newContent: The new page content to save back.
	 * @param newTags: The new page pages to save back.
	 * @return An enum that represents what happened. SaveResult.Success is returned if the save operation was successful, but otherwise the value indicates what kind of error was encountered.
	 */
	fun saveSource(pagename: String, editKey: String, newContent: String, newTags: String): WikiResult<Boolean?> {
		Log.i("Wiki", "Saving page content for $pagename with edit key $editKey")
		val response : WikiApiResponse = api.makePostRequest("save", mapOf(
			"page" to pagename
		), mapOf(
			"content" to newContent,
			"tags" to newTags,
			"prev-content-hash" to editKey
		)) ?: return WikiResult.Error(WikiError.NetworkError)
		
		
		val error : WikiError? = when(response.headers["x-failure-reason"]) {
			"editing-disabled" -> WikiError.NoEditsAllowedError
			"protected-page" -> WikiError.PageProtectedError
			"edit-conflict" -> WikiError.ConflictError
			"permissions-other-user-page" -> WikiError.PermissionsError
			else -> null
		}
		if(error != null) return WikiResult.Error(error)
		
		// If a login is required, this can either mean:
		// 1. The user is not logged in and anonymous editing is disabled
		// 2. The user IS logged in, but doesn't have enough privileges to save an edit
		// #2 will only be the case here with old servers that haven't yet been updated to return an x-failure-reason for protected page errors.
		if(response.isLoginRequired() && api.credentials == null)
			return WikiResult.Error(WikiError.PermissionsError)
		
		if(response.statusCode in 200..399)
			return WikiResult(Source.Internet, true)
		
		return WikiResult.Error(when(response.statusCode) {
			in 400..499 -> WikiError.UnknownClientError
			in 500..599 -> WikiError.ServerError
			else -> WikiError.UnknownError
		})
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