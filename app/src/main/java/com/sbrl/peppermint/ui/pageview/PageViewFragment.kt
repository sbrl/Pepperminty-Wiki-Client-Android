package com.sbrl.peppermint.ui.pageview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.PageHTMLProcessor
import com.sbrl.peppermint.ui.PageViewModel
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class PageViewFragment : Fragment() {
	private lateinit var pageViewModel: PageViewModel
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var webview: WebView
	
	private lateinit var pageHTMLProcessor: PageHTMLProcessor
	private var jsInterface = ViewPageJSInterface(this)
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// 1: Fetch the ViewModels
		// Each context gets it's own ViewModel instance
		pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
		wikiViewModel = ViewModelProvider(requireActivity()).get(WikiViewModel::class.java)
		
		// 2: Inflate the layout
		val root = inflater.inflate(R.layout.fragment_pageview, container, false)
		webview = root.findViewById(R.id.webview_pageview)
		pageHTMLProcessor = PageHTMLProcessor(requireContext())
		
		// 3: Initial UI updates
		activity?.title = pageViewModel.currentPageName.value
		
		// 4: Listeners
		pageViewModel.currentPageName.observe(viewLifecycleOwner, Observer {
			setPage(it)
		})
		
		return root
	}
	
	/**
	 * Pushes a page onto the stack
	 * @param pagename: The name of the page to push onto the stack.
	 * @param section: The name of the section on the page to jump to.
	 */
	fun pushPage(pagename: String, section: String = "") {
		// TODO: Handle page sections to jump directly to sections of pages
		pageViewModel.pushPage(pagename)
		/* Note that we do *not* call setPage directly here because the LiveData thing in the
		ViewModel will do this for us */
	}
	
	/**
	 * Updates the UI to display the content of the given page.
	 * @param pagename: The name of the page to fetch the content for and display.
	 */
	private fun setPage(pagename: String, section: String = "") {
		// TODO: Handle page sections to jump directly to sections of pages
		thread {
			val pageContent = wikiViewModel.currentWiki.value!!.pageContent(pagename)
			
			activity?.runOnUiThread {
				if(pageContent == null) {
					Toast.makeText(context,
						getString(R.string.error_failed_load_page_content, pagename),
						Toast.LENGTH_SHORT).show()
					return@runOnUiThread
				}
				
				activity?.title = pagename
				displayContent(pageContent)
			}
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private fun displayContent(contentHTML: String) {
		val displayHTML = pageHTMLProcessor.transform(contentHTML)
		// Configure the WebView
		webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
		webview.settings.javaScriptEnabled = true
		webview.settings.domStorageEnabled = false // Required for displaying images
		webview.settings.loadsImagesAutomatically = true
//		when(prefs.GetImageLoadingType()) {
//			"always" -> true
//			"wifi_only" -> is_wifi_enabled(this)
//			"never" -> false
//			else -> false
//		}
		webview.settings.databaseEnabled = false
		webview.settings.javaScriptCanOpenWindowsAutomatically = false
		webview.settings.mediaPlaybackRequiresUserGesture = true
		webview.settings.allowFileAccess = false
		webview.settings.allowFileAccessFromFileURLs = false
		webview.settings.allowUniversalAccessFromFileURLs = false
		
		webview.addJavascriptInterface(jsInterface, "App")
		
		// Sort out the cookies so that the webview can fetch images
		val endpoint = wikiViewModel.currentWiki.value!!.api.endpoint
		val webviewCookies = CookieManager.getInstance()
		for(cookie in wikiViewModel.currentWiki.value!!.api.getCookies()) {
			Log.d("PageViewFragment", "Adding cookie '${cookie.toString()}'")
			webviewCookies.setCookie(endpoint, cookie.toString())
		}
		
		// Load the data into the webview
		webview.loadDataWithBaseURL(
			endpoint,
			contentHTML,
			"text/html",
			"UTF-8",
			null
		)
	}
}