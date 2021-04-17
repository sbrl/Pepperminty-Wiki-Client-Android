package com.sbrl.peppermint.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.adapters.PageListAdapter
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class PageListFragment : Fragment() {
    
    private lateinit var root: View

    private lateinit var wikiviewModel: WikiViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Fetch the wiki view model containing the wiki manager
        wikiviewModel =
                ViewModelProvider(this).get(WikiViewModel::class.java)
        wikiviewModel.init(context)
        
        root = inflater.inflate(R.layout.fragment_pagelist, container, false)
        
        updatePageList()
        
        return root
    }
    
    /**
     * Fetches a page list using the current wiki and updates the currently displayed page list.
     */
    private fun updatePageList() {
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
                viewPageList.adapter = PageListAdapter(context ?: return@runOnUiThread, pageList)
                viewPageList.setHasFixedSize(true) // Apparently improves performance
            }
        }
        
    }
}