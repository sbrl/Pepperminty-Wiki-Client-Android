package com.sbrl.peppermint.ui.managewikis

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.send_add_wiki
import com.sbrl.peppermint.lib.ui.show_toast
import com.sbrl.peppermint.ui.WikiViewModel
import com.sbrl.peppermint.ui.adapters.WikiListAdapter

class ManageWikisActivity : AppCompatActivity() {
	
	lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var swipeRefresh: SwipeRefreshLayout
	private var wikiListAdapter: WikiListAdapter? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		// 1: Preamble
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_manage_wikis)
		
		// 2: View models
		wikiViewModel = ViewModelProvider(this)[WikiViewModel::class.java]
		wikiViewModel.init(this)
		
		// 3: Find views
		swipeRefresh = findViewById(R.id.swipe_refresh_wikilist)
		
		findViewById<Button>(R.id.wikilist_button_addwiki).setOnClickListener {
			uiLaunchAddWiki()
		}
		
		// Swipe-to-refresh
		swipeRefresh.setOnRefreshListener {
			updateWikiList()
		}
		
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		updateWikiList()
	}
	
	override fun onResume() {
		wikiViewModel.wikiManager.value!!.reloadFromDisk()
		updateWikiList()
		super.onResume()
	}
	
	private fun uiLaunchAddWiki() {
		send_add_wiki(this, clear_stack = false)
	}
	
	private fun uiStartWikiListRefresh() {
		swipeRefresh.isRefreshing = true
	}
	private fun uiFinishWikiListRefresh() {
		swipeRefresh.isRefreshing = false
	}
	
	
	private fun updateWikiList() {
		
		uiStartWikiListRefresh()
		
		Log.i("ActivityManageWikis", "Updating wiki list")
		
		val viewWikiList = findViewById<RecyclerView>(R.id.wikilist_list)
		
		val wikiManager = wikiViewModel.wikiManager.value
		if(wikiManager == null) {
			uiFinishWikiListRefresh()
			return
		}
		
		val wikiList = wikiManager.getWikiList().values.toList()
		
		Log.i("ActivityManageWikis", "Wiki list update: ${wikiList.count()} wikis present")
		
		wikiListAdapter?.itemSelectedRemove?.off(::wikiListAdapterSelectionRemoveHandler)
		wikiListAdapter = WikiListAdapter(this, wikiList)
		wikiListAdapter?.itemSelectedRemove?.on(::wikiListAdapterSelectionRemoveHandler)
		
		viewWikiList.adapter = wikiListAdapter
		viewWikiList.setHasFixedSize(true) // Apparently improves performance
		
		Log.i("ActivityManageWikis", "Wiki list update complete")
		
		uiFinishWikiListRefresh()
	}
	
	private fun wikiListAdapterSelectionRemoveHandler(_source: WikiListAdapter, args: WikiListAdapter.ItemSelectedRemoveEventArgs) {
		val wikiManager = wikiViewModel.wikiManager.value!!
		
		wikiManager.removeWiki(args.wiki)
		
		updateWikiList()
		
		show_toast(this, getString(R.string.managewikis_action_wiki_wikiname_removed).replace("{0}", args.wiki.name))
	}
	
}