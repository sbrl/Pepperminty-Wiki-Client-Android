package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.webkit.WebView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.Wiki
import kotlin.concurrent.thread

class ViewPage : TemplateNavigation() {
	
	private val INTENT_PARAM_WIKI_NAME = "wiki-name"
	private val INTENT_PARAM_PAGE_NAME = "page-name"
	
	private lateinit var pageName : String
	private lateinit var wiki : Wiki
	private lateinit var pageDisplay : WebView
	
	protected override val contentId : Int = R.layout.activity_view_page
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
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
		toolbar.title = "$newPageName - ${wiki.Name}"
		// Fetch and load the new page's HTML into the WebView
		val pageHTML = wiki.GetPageHTML(newPageName, refreshFromInternet)
		val encodedPageHTML = Base64.encodeToString(pageHTML.toByteArray(), Base64.DEFAULT)
		
		runOnUiThread {
			pageDisplay.loadData(encodedPageHTML, "text/html", "base64")
		}
	}
	
	// -----------------------------------------------------------------------------
	
	override fun changeWiki(wikiName: String) {
		// If the selected wiki is the one that's currently open, then just exit this
		// activity
		if(wikiName == wiki.Name)
			finish()
		
		// If not, then push a new one onto the stack.
		val intent = Intent(this, Main::class.java)
		intent.putExtra("wiki-name", wikiName)
		startActivity(intent)
	}
	
	// -----------------------------------------------------------------------------
}
