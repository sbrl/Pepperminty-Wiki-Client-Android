package com.sbrl.peppermint.ui.addwiki

import android.content.Context
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.ui.WikiViewModel

class LoginManager(private val context: Context, private val wikiViewModel: WikiViewModel) {
	
	fun doLogin(url: String, username: String, password: String) {
		val wiki = wikiViewModel.wikiManager.c
	}
}