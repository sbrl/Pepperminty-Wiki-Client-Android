package com.sbrl.peppermint.ui.pagelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.show_toast
import com.sbrl.peppermint.lib.ui.view_page
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.ui.adapters.PageListAdapter
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class PageListFragment : Fragment() {
	
	private lateinit var root: View
	
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var swipeRefresh: SwipeRefreshLayout
	private lateinit var searchFilter: SearchView
	
	private var pageListAdapter: PageListAdapter? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// 1: Fetch the wiki view model containing the wiki manager
		wikiViewModel =
			ViewModelProvider(requireActivity())[WikiViewModel::class.java]
		wikiViewModel.init(context)
		wikiViewModel.currentWiki.observe(viewLifecycleOwner) {
			Log.i("PageListFragment", "Current wiki changed, updating page list")
			updatePageList()
		}
		
		// 2: Inflate the layout, attach listeners
		root = inflater.inflate(R.layout.fragment_pagelist, container, false)
		
		// 3: Find views
		swipeRefresh = root.findViewById(R.id.swipe_refresh_pagelist)
		searchFilter = root.findViewById(R.id.pagelist_filter)
		
		
		// Swipe-to-refresh
		swipeRefresh.setOnRefreshListener {
			updatePageList()
		}
		
		searchFilter.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextChange(query : String?): Boolean {
				pageListAdapter?.filter?.filter(query)
				return true
			}
			override fun onQueryTextSubmit(query : String?): Boolean {
				pageListAdapter?.filter?.filter(query)
				return true
			}
		})
		
		// 3: Fill in the UI
		// Doesn't seem to be needed since the WikiViewModel observe call seems to always fire at least once
		//updatePageList()
		
		return root
	}
	
	private fun uiStartPageListRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishPageListRefresh(fromCache: Boolean) {
		swipeRefresh.isRefreshing = false
		if (fromCache) {
			val message = getString(R.string.toast_page_list_refreshed) + " " +
				getString(R.string.toast_addon_from_cache)
			
			show_toast(context, message)
		}
	}
	
	/**
	 * Fetches a page list using the current wiki and updates the currently displayed page list.
	 */
	private fun updatePageList() {
		uiStartPageListRefresh()
		
		Log.i("PageListFragment", "Updating page list")
		val viewPageList: RecyclerView = root.findViewById(R.id.pagelist_list)
		
		// Fetching the current wiki has to be on the ui thread to get the latest value, apparently
		val currentWiki = wikiViewModel.currentWiki.value ?: return
		
		Log.i("PageListFragment", "Current wiki is ${currentWiki.name}")
		
		// Fetching the page list might block for the network - spawn a thread
		// Non-blocking Kotlin is *hard* :-(
		thread {
			// Fetch a new page list
			val pageList: Wiki.WikiResult<List<String>> = currentWiki.pages() ?: return@thread
			
			
			// Create a new adapter, and tell the RecyclerView about it on the main thread
			activity?.runOnUiThread {
				// Attach / detach event listeners
				pageListAdapter?.itemSelected?.off(::pageListAdapterSelectionHandler)
				pageListAdapter = PageListAdapter(context ?: return@runOnUiThread, pageList.value)
				pageListAdapter?.itemSelected?.on(::pageListAdapterSelectionHandler)
				
				viewPageList.adapter = pageListAdapter
				viewPageList.setHasFixedSize(true) // Apparently improves performance
				
				
				uiFinishPageListRefresh(pageList.source == Wiki.Source.Cache)
			}
		}
		
	}
	private fun pageListAdapterSelectionHandler(_source: PageListAdapter, args: PageListAdapter.ItemSelectedEventArgs) {
		view_page(context, wikiViewModel, args.pagename)
	}
}