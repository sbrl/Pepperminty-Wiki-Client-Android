package com.sbrl.peppermint.ui.pageview

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import com.sbrl.peppermint.lib.PageHTMLProcessor
import com.sbrl.peppermint.lib.events.EventManager
import com.sbrl.peppermint.lib.io.SettingsManager
import com.sbrl.peppermint.ui.WikiViewModel

class WebViewManager(internal val context: Context, private val webview: WebView, private val wikiViewModel: WikiViewModel) {
	
	private var settings = SettingsManager(context)
	
	private var pageHTMLProcessor = PageHTMLProcessor(context)
	private var jsInterface = ViewPageJSInterface(this)
	
	data class OnLinkClickedEventArgs(val pagename: String, val sectionname: String)
	val onLinkClicked = EventManager<WebViewManager, OnLinkClickedEventArgs>("WebViewManager:onLinkClicked")
	
	/**
	 * Displays the given string of HTML in the webview.
	 * Santises HTML (e.g. removes JS), and wraps it in a template with some custom JS to manage
	 * interfacing.
	 * Best used for display wiki page content.
	 */
	@SuppressLint("SetJavaScriptEnabled")
	fun displayContent(contentHTML: String) {
		val displayHTML = pageHTMLProcessor.transform(contentHTML)
		// Configure the WebView
		webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
		webview.settings.javaScriptEnabled = true
		webview.settings.domStorageEnabled = false // Required for displaying images
		webview.settings.loadsImagesAutomatically = when(settings.load_images) {
			SettingsManager.LoadImages.Always -> true
			SettingsManager.LoadImages.OnlyOverWiFi -> is_wifi_enabled(context)
			SettingsManager.LoadImages.Never -> false
		}
		webview.settings.databaseEnabled = false
		webview.settings.javaScriptCanOpenWindowsAutomatically = false
		webview.settings.mediaPlaybackRequiresUserGesture = true
		webview.settings.allowFileAccess = false
//		webview.settings.allowFileAccessFromFileURLs = false
//		webview.settings.allowUniversalAccessFromFileURLs = false
		
		webview.addJavascriptInterface(jsInterface, "App")
		
		// Sort out the cookies so that the webview can fetch images
		val endpoint = wikiViewModel.currentWiki.value!!.api.endpoint
		val webviewCookies = CookieManager.getInstance()
		for(cookie in wikiViewModel.currentWiki.value!!.api.getCookies()) {
//			Log.d("PageViewFragment", "Adding cookie '${cookie.toString()}'")
			webviewCookies.setCookie(endpoint, cookie.toString())
		}
		
		
		// Load the data into the webview
		webview.loadDataWithBaseURL(
			endpoint,
			displayHTML,
			"text/html",
			"UTF-8",
			null
		)
	}
	
	/**
	 * Determines if the WiFi is enabled on the target device or not.
	 * @param context: The context to use to determine if the WiFi is enabled or not.
	 */
	private fun is_wifi_enabled(context : Context) : Boolean {
		return (context.applicationContext
			.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
	}
}