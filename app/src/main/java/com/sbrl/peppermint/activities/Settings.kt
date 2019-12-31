package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import com.sbrl.peppermint.R
import android.preference.ListPreference
import android.preference.Preference
import android.content.SharedPreferences



class Settings : TemplateNavigation() {
	override val contentId: Int = R.layout.activity_settings
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Don't need setContentView() here 'cause TemplateNavigation does it for us
	}
	
	override fun onWikiChangePre(wiki_name: String) {
		masterView.closeDrawers()
	}
	
	override fun onWikiChangePost(wiki_name: String) {
		// We're on the settings screen, so just launch a new activity
		// FUTURE: We might want to replace our current activity instead of launching a new one
		val intent = Intent(this, Main::class.java)
		intent.putExtra("wiki-name", wiki_name)
		startActivity(intent)
	}
}
