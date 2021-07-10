package com.sbrl.peppermint.ui.addwiki

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager
import com.sbrl.peppermint.lib.polyfill.is_valid_url
import com.sbrl.peppermint.lib.polyfill.make_id
import com.sbrl.peppermint.lib.wiki_api.ConnectionStatus
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.lib.wiki_api.WikiCredentials
import com.sbrl.peppermint.ui.WikiViewModel

class AddWikiManager(private val context: Context, private val wikiViewModel: WikiViewModel) {
	
	/**
	 * Tests the given wiki settings to ensure it's a valid wiki.
	 * @param endpoint: The URL stem that the wiki can be reached at.
	 * @param username: Optional. The username to login with.
	 * @param password: Optional. The password to login with.
	 */
	fun testSettings(endpoint: String, username: String?, password: String?) : ConnectionStatus {
		if(!is_valid_url(endpoint)) return ConnectionStatus.ConnectionFailed
		val wiki = createWiki(
			"__login_test_wiki_${make_id(16)}",
			endpoint,
			username,
			password
		)
		
		return wiki.connectionStatus()
	}
	fun testSettings(endpoint: String) : ConnectionStatus { return testSettings(endpoint, null, null); }
	
	/**
	 * Adds a wiki to the current wiki manager.
	 */
	fun addWiki(wiki: Wiki) {
		// This also saves it to disk
		if(!wikiViewModel.wikiManager.value!!.addWiki(wiki))
			Log.e("AddWikiManager", "Error: Wiki.addWiki returned false!")
	}
	
	/**
	 * Creates a new wiki instance.
	 * @param name: The display name of the wiki.
	 * @param endpoint: The URL stem that the wiki can be reached at.
	 * @param username: Optional. The username to login with.
	 * @param password: Optional. The password to login with.
	 */
	fun createWiki(name: String, endpoint: String, username: String?, password: String?) : Wiki {
		val credentials = if(username !== null && password !== null) WikiCredentials(username, password)
		else null
		return wikiViewModel.wikiManager.value!!.createWiki(
			name,
			endpoint,
			credentials
		)
	}
	fun createWiki(name: String, endpoint: String) : Wiki { return createWiki(name, endpoint, null, null); }
}