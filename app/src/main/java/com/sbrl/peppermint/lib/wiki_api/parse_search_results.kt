package com.sbrl.peppermint.lib.wiki_api

import org.json.JSONObject

fun parse_search_result(searchResult: JSONObject): WikiSearchResult {
	val pagename = searchResult.getString("pagename")
	val rank = searchResult.getInt("rank")
	val rankTitle = searchResult.getInt("rank_title")
	val rankTags = searchResult.getInt("rank_tags")
	val context = searchResult.getString("context")
	// There's more we could extract here, but I'm not sure it's needed juuuust yet
	
	return WikiSearchResult(
		pagename,
		rank,
		rankTitle,
		rankTags,
		context
	)
}

/**
 * Parses a string of JSON into a list of WikiSearchResult objects.
 * @param	jsonText: The string of JSON to parse.
 * @return	An ordered list of WikiSearchResult objects.
 */
fun parse_search_results(jsonText: String): List<WikiSearchResult> {
	val result = mutableListOf<WikiSearchResult>()
	val searchResults = JSONObject(jsonText)
	
	for(searchResultPagename in searchResults.keys()) {
		val searchResult = searchResults.getJSONObject(searchResultPagename)
		result.add(parse_search_result(searchResult))
	}
	
	return result
}