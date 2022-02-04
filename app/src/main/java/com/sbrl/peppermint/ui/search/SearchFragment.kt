package com.sbrl.peppermint.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.view_page
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.lib.wiki_api.WikiRecentChange
import com.sbrl.peppermint.lib.wiki_api.WikiSearchResult
import com.sbrl.peppermint.ui.WikiViewModel
import com.sbrl.peppermint.ui.adapters.SearchResultsListAdapter
import kotlin.concurrent.thread

class SearchFragment : Fragment() {
	
	private lateinit var root: View
	
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var query: SearchView
	private lateinit var swipeRefresh: SwipeRefreshLayout
	private lateinit var resultslist: RecyclerView
	
	private var searchResultsListAdapter: SearchResultsListAdapter? = null
	
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
			Log.i("RecentChangesFragment", "Current wiki changed, updating recent changes list")
			updateSearchResultsList()
		}
		
		// 2: Inflate the layout, attach listeners
		root = inflater.inflate(R.layout.fragment_search, container, false)
		
		// 3: Find views
		query = root.findViewById(R.id.search_query)
		swipeRefresh = root.findViewById(R.id.swipe_refresh_searchlist)
		resultslist = root.findViewById(R.id.search_list)
		
		// Swipe-to-refresh
		swipeRefresh.setOnRefreshListener {
			Log.i("SearchFragment", "Swipe refresh")
			updateSearchResultsList()
		}
		query.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(newText : String?): Boolean {
				query.clearFocus()
				updateSearchResultsList()
				return true
			}
			
			override fun onQueryTextChange(newText: String?): Boolean {
				// noop
				return true
			}
		})
		
		return root
		
		// 4: Fill in the UI
		// Doesn't seem to be needed since the WikiViewModel observe call seems to always fire at least once
		//updateSearchResultsList()
	}
	
	private fun uiStartSearchResultsListRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishSearchResultsListRefresh() {
		swipeRefresh.isRefreshing = false
	}
	
	private fun updateSearchResultsList() {
		uiStartSearchResultsListRefresh()
		
		// Fetching the current wiki has to be on the ui thread to get the latest value, apparently
		val currentWiki = wikiViewModel.currentWiki.value ?: return
		Log.i("SearchFragment", "updateSearchResultsList: current wiki is ${currentWiki.name}")
		
		thread {
			val queryText: String = query.query.toString()
			
			var searchResults = listOf<WikiSearchResult>()
			
			if(queryText.isNotEmpty()) {
				Log.i("SearchFragment", "Searching for query '$queryText'")
				val searchResultsWrapped: Wiki.WikiResult<List<WikiSearchResult>> =
					currentWiki.search(queryText) ?: return@thread
				
				searchResults = searchResultsWrapped.value
			}
			
			
			activity?.runOnUiThread {
				// Attach / detach event listeners
				searchResultsListAdapter?.itemSelected?.off(::searchResultsListAdapterSelectionHandler)
				searchResultsListAdapter = SearchResultsListAdapter(context ?: return@runOnUiThread, searchResults)
				searchResultsListAdapter?.itemSelected?.on(::searchResultsListAdapterSelectionHandler)
				
				resultslist.adapter = searchResultsListAdapter
				resultslist.setHasFixedSize(true) // Apparently improves performance
				
				
				uiFinishSearchResultsListRefresh()
			}
		}
	}
	
	private fun searchResultsListAdapterSelectionHandler(_source: SearchResultsListAdapter, args: SearchResultsListAdapter.ItemSelectedEventArgs) {
		view_page(context, wikiViewModel, args.search_result.pagename)
	}
}