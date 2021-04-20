package com.sbrl.peppermint.ui.pageview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.AbsSpinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.PageHTMLProcessor
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.ui.PageViewModel
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class PageViewFragment : Fragment() {
	private lateinit var pageViewModel: PageViewModel
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var webviewManager: WebViewManager
	
	private lateinit var swipeRefresh: SwipeRefreshLayout
	
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
		swipeRefresh = root.findViewById(R.id.swipe_refresh_pageview)
		
		
		// 3: Initial UI updates & webview manager
		activity?.title = pageViewModel.currentPageName.value
		
		// The WebviewManager is responsible for managing the web view. Note that the page stack
		// is managed in this fragment and not webview manager.
		webviewManager = WebViewManager(
			requireContext(),
			root.findViewById(R.id.webview_pageview),
			wikiViewModel
		)
		webviewManager.onLinkClicked.on { _source, args ->
			pushPage(args.pagename, args.sectionname)
		}
		
		
		// 4: Listeners
		pageViewModel.currentPageName.observe(viewLifecycleOwner, Observer {
			setPage(it)
		})
		swipeRefresh.setOnRefreshListener {
			pageViewModel.currentPageName.value?.let { setPage(it) }
		}
		
		return root
	}
	
	private fun uiStartPageViewRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishPageViewRefresh(fromCache: Boolean) {
		val message = getString(R.string.toast_page_view_refreshed) + " " +
			(if(fromCache) getString(R.string.toast_addon_from_cache)
				else getString(R.string.toast_addon_from_internet))
		
		swipeRefresh.isRefreshing = false
		Toast.makeText(context,
			message,
			Toast.LENGTH_SHORT).show()
	}
	
	/**
	 * Pushes a page onto the stack
	 * @param pagename: The name of the page to push onto the stack.
	 * @param section: The name of the section on the page to jump to.
	 */
	private fun pushPage(pagename: String, section: String = "") {
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
		uiStartPageViewRefresh()
		// TODO: Handle page sections to jump directly to sections of pages
		thread {
			val pageContent = wikiViewModel.currentWiki.value!!.pageContent(pagename)
			
			activity?.runOnUiThread {
				if(pageContent == null) {
					Toast.makeText(context,
						getString(R.string.error_failed_load_page_content, pagename),
						Toast.LENGTH_SHORT).show()
					swipeRefresh.isRefreshing = false
					return@runOnUiThread
				}
				
				// Ref https://stackoverflow.com/a/54893709/1460422
				(requireActivity() as AppCompatActivity).supportActionBar?.title = pagename
				webviewManager.displayContent(pageContent.value)
				
				uiFinishPageViewRefresh(pageContent.source == Wiki.Source.Cache)
			}
		}
	}
	
}