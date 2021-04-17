package com.sbrl.peppermint.ui.pagelist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.EXTRA_PAGE_NAME
import com.sbrl.peppermint.ui.EXTRA_WIKI_NAME
import com.sbrl.peppermint.ui.PageActivity
import com.sbrl.peppermint.ui.adapters.PageListAdapter
import com.sbrl.peppermint.ui.WikiViewModel
import kotlinx.coroutines.delay
import kotlin.concurrent.thread

class PageListFragment : Fragment() {
	
	private lateinit var root: View
	
	private lateinit var wikiviewModel: WikiViewModel
	
	private lateinit var swipeRefresh: SwipeRefreshLayout
	
	private var pageListAdapter: PageListAdapter? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// 1: Fetch the wiki view model containing the wiki manager
		wikiviewModel =
			ViewModelProvider(requireActivity()).get(WikiViewModel::class.java)
		wikiviewModel.init(context)
		
		// 2: Inflate the layout, attach listeners
		root = inflater.inflate(R.layout.fragment_pagelist, container, false)
		
		// Swipe-to-refresh
		swipeRefresh = root.findViewById(R.id.swipe_refresh_pagelist)
		swipeRefresh.setOnRefreshListener {
			updatePageList()
		}
		
		
		// 3: Fill in the UI
		updatePageList()
		
		return root
	}
	
	private fun uiStartPageListRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishPageListRefresh(fromCache: Boolean) {
		val message = getString(R.string.toast_page_list_refreshed) + " " +
			(if(fromCache) getString(R.string.toast_addon_from_cache)
			else getString(R.string.toast_addon_from_internet))
		
		swipeRefresh.isRefreshing = false
		Toast.makeText(context,
			message,
			Toast.LENGTH_SHORT).show()
	}
	
	/**
	 * Fetches a page list using the current wiki and updates the currently displayed page list.
	 */
	private fun updatePageList() {
		uiStartPageListRefresh()
		
		Log.i("PageListFragment", "Updating page list")
		val viewPageList: RecyclerView = root.findViewById(R.id.pagelist_list)
		
		// Fetching the current wiki has to be on the ui thread to get the latest value, apparently
		val currentWiki = wikiviewModel.currentWiki.value ?: return
		
		Log.i("PageListFragment", "Current wiki is ${currentWiki.name}")
		
		// Fetching the page list might block for the network - spawn a thread
		// Non-blocking Kotlin is *hard* :-(
		thread {
			// Fetch a new page list
			val pageList: List<String> = currentWiki.pages() ?: return@thread
			// Create a new adapter, and tell the RecyclerView about it on the main thread
			activity?.runOnUiThread {
				// Attach / detach event listeners
				pageListAdapter?.itemSelected?.off(::pageListAdapterSelectionHandler)
				pageListAdapter = PageListAdapter(context ?: return@runOnUiThread, pageList)
				pageListAdapter?.itemSelected?.on(::pageListAdapterSelectionHandler)
				
				viewPageList.adapter = pageListAdapter
				viewPageList.setHasFixedSize(true) // Apparently improves performance
				
				
				uiFinishPageListRefresh(false)
			}
		}
		
	}
	
	private fun pageListAdapterSelectionHandler(source: PageListAdapter, args: PageListAdapter.ItemSelectedEventArgs) {
		viewPage(args.pagename)
	}
	
	/**
	 * Opens an activity to view the current page.
	 * This has to be here because ViewModels are per-activity or per-fragment :-/
	 * @param pagename: The name of the page to view.
	 */
	private fun viewPage(pagename: String) : Boolean {
		val currentWiki = wikiviewModel.currentWiki.value ?: return false
		val ctx = context ?: return false
		
		val intent = Intent(ctx, PageActivity::class.java).apply {
			putExtra(EXTRA_WIKI_NAME, currentWiki.id)
			putExtra(EXTRA_PAGE_NAME, pagename)
		}
		
		ctx.startActivity(intent)
		return true
	}
}