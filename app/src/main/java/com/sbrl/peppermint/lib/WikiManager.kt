package com.sbrl.peppermint.lib

class WikiManager {
	private var currentWiki: Wiki? = null;

	/**
	 * Loads a new instance of the wiki with the given ID.
	 * @param id: The ID of the wiki to load.
	 */
	private fun getWiki(id: String) : Wiki {

	}


	/**
	 * Sets the current wiki.
	 * @param id: The ID of the wiki to switch to.
	 * @return Boolean: Whether the operation was successful or not (e.g. if the wiki doesn't exist, we can't switch to it)
	 */
	fun setWiki(id: String): Boolean {
		currentWiki = getWiki(id)
	}
}