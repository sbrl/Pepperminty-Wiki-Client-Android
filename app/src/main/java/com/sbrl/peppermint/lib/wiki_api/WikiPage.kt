package com.sbrl.peppermint.lib.wiki_api

class WikiPage(val content: String, val tags: List<String>) {
	
	/**
	 * The tag list serialised to a string.
	 * This value is identical to that required by the Pepperminty Wiki Rest API.
	 */
	val tagsAsString: String
		get() = tags.joinToString(", ")
}