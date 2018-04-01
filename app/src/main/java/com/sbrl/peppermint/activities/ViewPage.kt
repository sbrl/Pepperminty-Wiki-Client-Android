package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.PageHTMLProcessor
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.Wiki
import kotlin.concurrent.thread
import kotlin.text.toByteArray

class ViewPage : TemplateNavigation() {
	private val LogTag = "[activity] ViewPage"
	
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
		
		thread(start = true) {
			wiki = Wiki(
				this,
				intent.getStringExtra(INTENT_PARAM_WIKI_NAME),
				prefs.GetCredentials(intent.getStringExtra(INTENT_PARAM_WIKI_NAME)),
				false
			)
			
			showPage(pageName, false)
		}
	}
	
	override fun onResume() {
		super.onResume()
		
		setSelectedWiki(intent.getStringExtra(INTENT_PARAM_WIKI_NAME))
	}
	
	/* ********************************************************************** */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.view_page_options, menu)
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem) : Boolean = when(item.itemId) {
		R.id.view_page_menu_refresh -> {
			thread(start = true) {
				showPage(pageName, true)
			}
			true
		}
		else -> super.onOptionsItemSelected(item)
	}
	/* ********************************************************************** */
	
	private fun showPage(newPageName : String, refreshFromInternet : Boolean) {
		// Update the current page name
		pageName = newPageName
		
		// Fetch and load the new page's HTML into the WebView
		val pageProcessor = PageHTMLProcessor(this)
		val pageHTML = pageProcessor.transform(
			wiki.GetPageHTML(newPageName, refreshFromInternet)
		)
		//val encodedPageHTML = "data:" + Base64.encodeToString(pageHTML.toByteArray(), Base64.DEFAULT)
		
		runOnUiThread {
			// Configure the WebView
			pageDisplay.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
			pageDisplay.settings.javaScriptEnabled = true
			pageDisplay.settings.domStorageEnabled = false // Required for displaying images
			pageDisplay.settings.loadsImagesAutomatically = true
			pageDisplay.settings.databaseEnabled = false
			pageDisplay.settings.javaScriptCanOpenWindowsAutomatically = false
			pageDisplay.settings.mediaPlaybackRequiresUserGesture = true
			pageDisplay.settings.allowFileAccess = false
			pageDisplay.settings.allowFileAccessFromFileURLs = false
			pageDisplay.settings.allowUniversalAccessFromFileURLs = false
			
			// Give the WebView the authentication cookie
			val cookieManager = CookieManager.getInstance()
			cookieManager.setCookie(wiki.Info.RootUrl, "${wiki.LoginCookieName}=${prefs.GetSessionToken()}; path=/")
			
			Log.i(LogTag, "Base url: ${wiki.Info.RootUrl}")
			pageDisplay.loadDataWithBaseURL(
				wiki.Info.RootUrl,
				pageHTML,
				"text/html",
				"UTF-8",
				null
			)
			toolbar.title = "$newPageName - ${wiki.Name}"
		}
	}
	
	// -----------------------------------------------------------------------------
	
	override fun changeWiki(wikiName: String) {
		// If the selected wiki is the one that's currently open, then just exit this
		// activity
		if(wikiName == wiki.Name)
			finish()
		
		masterView.closeDrawers()
		
		// If not, then push a new one onto the stack.
		val intent = Intent(this, Main::class.java)
		intent.putExtra("wiki-name", wikiName)
		startActivity(intent)
	}
	
	// -----------------------------------------------------------------------------
}
