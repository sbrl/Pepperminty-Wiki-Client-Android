package com.sbrl.peppermint.lib

import com.sbrl.peppermint.lib.events.EventManager
import com.sbrl.peppermint.lib.io.DataManager
import org.json.JSONArray
import org.json.JSONObject

class WikiManager(private val dataManager: DataManager) {
	var currentWiki: Wiki = defaultWiki()
		get() = field
		set(value) {
			field = value
			wikiChanged.emit(this, WikiChangedEventArgs(field))
		}
	
	/**
	 * Event that's fired when the current wiki changes.
	 */
	val wikiChanged: EventManager<WikiManager, WikiChangedEventArgs> = EventManager()
	class WikiChangedEventArgs(val newCurrentWiki: Wiki)
	
	private var wikis: MutableMap<String, Wiki>
	
	init {
		wikis = loadWikis()
		currentWiki = randomWiki()
	}
	
	/**
	 * Returns a random wiki.
	 */
	private fun randomWiki() : Wiki {
		return wikis[wikis.keys.asSequence().toList()[0]]!!
	}
	private fun defaultWiki() : Wiki {
		return Wiki("Test Wiki", "https://starbeamrainbowlabs.com/labs/peppermint/build/")
	}
	
	/**
	 * Determines whether a wiki exists with the given id or not.
	 * @param id: The id of the wiki to check.
	 */
	fun existsById(id: String) : Boolean {
		return wikis.containsKey(id)
	}
	
	/**
	 * Sets the current wiki.
	 * @param id: The ID of the wiki to switch to.
	 * @return Boolean: Whether the operation was successful or not (e.g. if the wiki doesn't exist, we can't switch to it)
	 */
	fun setWiki(id: String): Boolean {
		val newWiki = wikis[id] ?: return false
		currentWiki = newWiki
		return true
	}
	
	/**
	 * Adds a new wiki to the WikiManager.
	 */
	fun addWiki(id: String, wiki: Wiki) : Boolean {
		if(wikis[id] == null)
			return false
		wikis[id] = wiki
		
		saveWikis()
		return true
	}
	
	// --------------------------------------------------------------------------------------------
	
	/**
	 * Loads the wiki data from a JSON file on disk.
	 */
	private fun loadWikis() : MutableMap<String, Wiki> {
		val wikis = JSONObject(
			dataManager.getStoredString("meta", "wiki-list.json") ?: "{}"
		)
		val result: MutableMap<String, Wiki> = mutableMapOf()
		for(wikiId in wikis.keys())
			result[wikiId] = Wiki.load(wikis.getJSONObject(wikiId))
		
		// Make sure there is *always* at least 1 wiki
		if(result.count() == 0)
			result["__default"] = defaultWiki()
		return result
	}
	
	/**
	 * Saves the wiki data to a JSON file on disk.
	 */
	private fun saveWikis() {
		val result = JSONObject()
		for((id, wiki) in wikis)
			result.put(id, wiki.save())
		
		dataManager.storeString("meta", "wiki-list.json", result.toString())
	}
	
}