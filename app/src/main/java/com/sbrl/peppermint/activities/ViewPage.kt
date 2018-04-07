package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.PageHTMLProcessor
import com.sbrl.peppermint.bricks.is_wifi_enabled
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.bricks.readTextAndClose
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.display.ViewPageWebInterface
import kotlin.concurrent.thread

class ViewPage : TemplateNavigation() {
	private val LogTag = "[activity] ViewPage"
	
	public val INTENT_PARAM_WIKI_NAME = "wiki-name"
	public val INTENT_PARAM_PAGE_NAME = "page-name"
	public val INTENT_PARAM_PAGE_SECTION = "page-section"
	
	public val SPECIAL_PAGE_ID_CREDITS = "@@___credits"
	
	private lateinit var pageName : String
	private var wiki : Wiki? = null
	private lateinit var pageDisplay : WebView
	
	protected override val contentId : Int = R.layout.activity_view_page
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// --------------------------------------------
		
		pageDisplay = findViewById(R.id.view_page_html_display)
		
		prefs = PreferencesManager(this)
		pageName = intent.getStringExtra(INTENT_PARAM_PAGE_NAME)
		
		thread(start = true) {
			if(intent.hasExtra(INTENT_PARAM_WIKI_NAME))
				wiki = Wiki(
					this,
					intent.getStringExtra(INTENT_PARAM_WIKI_NAME),
					prefs.GetCredentials(intent.getStringExtra(INTENT_PARAM_WIKI_NAME)),
					false
				)
			
			ChangePage(pageName, false)
		}
	}
	
	override fun onResume() {
		super.onResume()
		
		setSelectedWiki(if(intent.hasExtra(INTENT_PARAM_WIKI_NAME))
			intent.getStringExtra(INTENT_PARAM_WIKI_NAME)
		else
			intent.getStringExtra(INTENT_PARAM_PAGE_NAME))
	}
	
	/* ********************************************************************** */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.view_page_options, menu)
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem) : Boolean = when(item.itemId) {
		R.id.view_page_menu_refresh -> {
			thread(start = true) {
				ChangePage(pageName, true)
			}
			true
		}
		else -> super.onOptionsItemSelected(item)
	}
	
	/* ********************************************************************** */
	
	public fun GetCurrentWikiName() : String = wiki?.Name ?: ""
	
	public fun NotifyRedlink(pageName : String) {
		runOnUiThread {
			notify_send(this, "$pageName doesn't exist.")
		}
	}
	
	/* ********************************************************************** */
	
	
	
	public fun ChangePage(newPageName : String, refreshFromInternet : Boolean) {
		// Update the current page name
		pageName = newPageName
		
		// Fetch and load the new page's HTML into the WebView
		val pageProcessor = PageHTMLProcessor(this)
		
		val pageHTML = pageProcessor.transform(
			getSpecialPageContent(newPageName) ?:
				wiki!!.GetPageHTML(newPageName, refreshFromInternet)
		)
		//val encodedPageHTML = "data:" + Base64.encodeToString(pageHTML.toByteArray(), Base64.DEFAULT)
		
		runOnUiThread {
			// Configure the WebView
			pageDisplay.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
			pageDisplay.settings.javaScriptEnabled = true
			pageDisplay.settings.domStorageEnabled = false // Required for displaying images
			pageDisplay.settings.loadsImagesAutomatically = when(prefs.GetImageLoadingType()) {
				"always" -> true
				"wifi_only" -> is_wifi_enabled(this)
				"never" -> false
				else -> false
			}
			pageDisplay.settings.databaseEnabled = false
			pageDisplay.settings.javaScriptCanOpenWindowsAutomatically = false
			pageDisplay.settings.mediaPlaybackRequiresUserGesture = true
			pageDisplay.settings.allowFileAccess = false
			pageDisplay.settings.allowFileAccessFromFileURLs = false
			pageDisplay.settings.allowUniversalAccessFromFileURLs = false
			
			Log.i(LogTag, "Loading images: ${pageDisplay.settings.loadsImagesAutomatically}")
			
			// Give the WebView the authentication cookie
			if(wiki != null) {
				val cookieManager = CookieManager.getInstance()
				cookieManager.setCookie(wiki!!.Info.RootUrl, "${wiki!!.LoginCookieName}=${prefs.GetSessionToken()}; path=/")
			}
			
			// Add the javascript interface
			pageDisplay.addJavascriptInterface(ViewPageWebInterface(this), "App")
			
			if(wiki != null) {
				Log.i(LogTag, "Base url: ${wiki!!.Info.RootUrl}")
				pageDisplay.loadDataWithBaseURL(
					wiki!!.Info.RootUrl,
					pageHTML,
					"text/html",
					"UTF-8",
					null
				)
			} else {
				pageDisplay.loadData(
					pageHTML,
					"text/html",
					"UTF-8"
				)
			}
			toolbar.title = when(newPageName) {
				SPECIAL_PAGE_ID_CREDITS -> "Credits"
				else -> "$newPageName - ${wiki!!.Name}"
			}
		}
	}
	
	private fun getSpecialPageContent(specialPageId : String) : String? {
		return when(specialPageId) {
			SPECIAL_PAGE_ID_CREDITS -> resources.openRawResource(R.raw.credits).readTextAndClose()
			else -> null
		}
	}
	
	// -----------------------------------------------------------------------------
	
	override fun changeWiki(wikiName: String) {
		// If the selected wiki is the one that's currently open, then just exit this
		// activity
		if(wikiName == wiki?.Name)
			finish()
		
		masterView.closeDrawers()
		
		// If not, then push a new one onto the stack.
		val intent = Intent(this, Main::class.java)
		intent.putExtra("wiki-name", wikiName)
		startActivity(intent)
	}
	
	// -----------------------------------------------------------------------------
}
