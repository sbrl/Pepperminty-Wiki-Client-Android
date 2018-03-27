package com.sbrl.peppermint.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.webkit.WebView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.Wiki
import kotlin.concurrent.thread

class ViewPage : AppCompatActivity() {
	private val INTENT_PARAM_WIKI_NAME = "wiki-name"
	private val INTENT_PARAM_PAGE_NAME = "page-name"
	
	private lateinit var prefs : PreferencesManager
	
	private lateinit var pageName : String
	private lateinit var wiki : Wiki
	private lateinit var pageDisplay : WebView
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_view_page)
		
		// --------------------------------------------
		
		pageDisplay = findViewById(R.id.view_page_html_display)
		
		prefs = PreferencesManager(this)
		pageName = intent.getStringExtra(INTENT_PARAM_PAGE_NAME)
		wiki = Wiki(
			this,
			intent.getStringExtra(INTENT_PARAM_WIKI_NAME),
			prefs.GetCredentials(intent.getStringExtra(INTENT_PARAM_WIKI_NAME))
		)
		
		thread(start = true) {
			showPage(pageName, false)
		}
	}
	
	private fun showPage(newPageName : String, refreshFromInternet : Boolean) {
		pageName = newPageName // Update the current page name
		// Fetch and load the new page's HTML into the WebView
		val pageHTML = wiki.GetPageHTML(newPageName, refreshFromInternet)
		val encodedPageHTML = Base64.encodeToString(pageHTML.toByteArray(), Base64.DEFAULT)
		
		runOnUiThread {
			pageDisplay.loadData(encodedPageHTML, "text/html", "base64")
		}
	}
}
