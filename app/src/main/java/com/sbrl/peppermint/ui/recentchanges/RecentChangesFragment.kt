package com.sbrl.peppermint.ui.recentchanges

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
import com.sbrl.peppermint.lib.wiki_api.WikiResult
import com.sbrl.peppermint.ui.MainActivity
import com.sbrl.peppermint.ui.WikiViewModel
import com.sbrl.peppermint.ui.adapters.RecentChangesListAdapter
import kotlin.concurrent.thread

class RecentChangesFragment : Fragment() {
	
	private lateinit var root: View
	
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var swipeRefresh: SwipeRefreshLayout
	private lateinit var searchFilter: SearchView
	
	private var recentChangesListAdapter: RecentChangesListAdapter? = null
	
	private val mainActivity: MainActivity
		get() = activity as MainActivity
	
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
			updateRecentChangesList()
		}
		
		
		// 2: Inflate the layout, attach listeners
		root = inflater.inflate(R.layout.fragment_recentchanges, container, false)
		
		
		// 3: Find views
		swipeRefresh = root.findViewById(R.id.swipe_refresh_recentchanges)
		searchFilter = root.findViewById(R.id.recentchanges_filter)
		
		
		// Swipe-to-refresh
		swipeRefresh.setOnRefreshListener {
			Log.i("RecentChangesFragment", "Swipe refresh")
			updateRecentChangesList()
		}
		searchFilter.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextChange(query : String?): Boolean {
				recentChangesListAdapter?.filter?.filter(query)
				return true
			}
			override fun onQueryTextSubmit(query : String?): Boolean {
				recentChangesListAdapter?.filter?.filter(query)
				return true
			}
		})
		
		
		// 4: Fill in the UI
		// Doesn't seem to be needed since the WikiViewModel observe call seems to always fire at least once
		//updateRecentChangesList()
		
		return root
	}
	
	private fun uiStartRecentChangesListRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishRecentChangesListRefresh(fromCache: Boolean) {
		val message = getString(R.string.toast_recentchanges_list_refreshed) + " " +
			(if(fromCache) getString(R.string.toast_addon_from_cache)
			else getString(R.string.toast_addon_from_internet))
		
		swipeRefresh.isRefreshing = false
		mainActivity.showSnackMessage(message)
	}
	
	private fun updateRecentChangesList() {
		uiStartRecentChangesListRefresh()
		
		val viewRecentChangesList: RecyclerView = root.findViewById(R.id.recentchanges_list)
		
		// Fetching the current wiki has to be on the ui thread to get the latest value, apparently
		val currentWiki = wikiViewModel.currentWiki.value ?: return
		
		Log.i("RecentChangesFragment", "updateRecentChangesList: current wiki is ${currentWiki.name}")
		
		// Fetching the page list might block for the network - spawn a thread
		// Non-blocking Kotlin is *hard* :-(
		thread {
			// Fetch a new page list
			val recentChangesList: WikiResult<List<WikiRecentChange>> =
				currentWiki.recentChanges() ?: return@thread
			
			
			// Create a new adapter, and tell the RecyclerView about it on the main thread
			activity?.runOnUiThread {
				// Attach / detach event listeners
				recentChangesListAdapter?.itemSelected?.off(::recentChangesListAdapterSelectionHandler)
				recentChangesListAdapter = RecentChangesListAdapter(context ?: return@runOnUiThread, recentChangesList.value)
				recentChangesListAdapter?.itemSelected?.on(::recentChangesListAdapterSelectionHandler)
				
				viewRecentChangesList.adapter = recentChangesListAdapter
				viewRecentChangesList.setHasFixedSize(true) // Apparently improves performance
				
				
				uiFinishRecentChangesListRefresh(recentChangesList.source == Wiki.Source.Cache)
			}
		}
		
	}
	
	private fun recentChangesListAdapterSelectionHandler(_source: RecentChangesListAdapter, args: RecentChangesListAdapter.ItemSelectedEventArgs) {
		// TODO: Implement viewing specific history revisions
		view_page(context, wikiViewModel, args.recentChange.pageName)
	}
}