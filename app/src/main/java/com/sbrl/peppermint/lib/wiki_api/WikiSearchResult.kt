package com.sbrl.peppermint.lib.wiki_api

/**
 * Represents a single search result.
 */
class WikiSearchResult(
	val pagename: String,
	val rank: Int,
	val rankTitle: Int,
	val rankTags: Int,
	val context: String
) {
	
}