package com.sbrl.peppermint.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbrl.peppermint.R

/**
 * Mediates between a dataset and a list of items being displlayed on the screen through a list of
 * views by way of a RecyclerView.
 * In this particular instance, we are displaying a list of page names.
 * @see https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#3
 */
class PageListAdapter (
	private val context: Context,
	private val dataset: List<String>
	) : RecyclerView.Adapter<PageListAdapter.PageItemHolder>() {
	
	/**
	 * Holds information about a single item that si being displayed in the list.
	 * RecyclerViews don't interact directly with the items in the list - preferring a
	 * helpepr class instance instead.
	 */
	class PageItemHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		/**
		 * The text box that holds the name of the page.
		 */
		val viewPageName: TextView = view.findViewById(R.id.pagelist_list_name)
	}
	
	/**
	 * Called when the RecyclerView wants to create a new item to display in the list
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageItemHolder {
		// Inflate a new instance of the view for this particular item
		val itemLayout = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_pagelist, parent, false)
		
		// Return a new Holder class instance
		return PageItemHolder(itemLayout)
	}
	
	/**
	 * Fills a previously created view for a single item in the list with the data for a
	 * specific item in the dataset.
	 * @param holder: The holder class instance to fill with the data from the dataset.
	 * @param i: The index of the item in the dataset that is being requested.
	 */
	override fun onBindViewHolder(holder: PageItemHolder, i: Int) {
		val item = dataset[i] // Find the item
		
		// Fill the holder with the item's data
		holder.viewPageName.text = item
		
		holder.itemView.setOnClickListener {
		
		}
	}
	
	/**
	 * Returns the number of items in the dataset.
	 */
	override fun getItemCount(): Int {
		return dataset.size
	}
}