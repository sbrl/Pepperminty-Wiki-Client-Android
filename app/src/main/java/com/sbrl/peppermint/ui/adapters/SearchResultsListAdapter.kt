package com.sbrl.peppermint.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.events.EventManager
import com.sbrl.peppermint.lib.wiki_api.WikiSearchResult
import java.util.*

/**
 * Mediates between a dataset and a list of items being displayed on the screen through a list of
 * views by way of a RecyclerView.
 * In this particular instance, we are displaying a list of search results.
 * @source https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#3
 */
class SearchResultsListAdapter(
	private val context: Context,
	private val raw_dataset: List<WikiSearchResult>,
) : RecyclerView.Adapter<SearchResultsListAdapter.SearchResultItemHolder>(), Filterable {
	
	private val dataset = mutableListOf<WikiSearchResult>()
	
	init {
		dataset.addAll(raw_dataset)
	}
	
	class ItemSelectedEventArgs(val search_result: WikiSearchResult)
	
	val itemSelected: EventManager<SearchResultsListAdapter, ItemSelectedEventArgs> = EventManager("SearchResultsListAdapter:itemSelected")
	
	class SearchResultItemHolder(view: View) : RecyclerView.ViewHolder(view) {
		/**
		 * The icon in the top left of the card
		 */
		val icon: ImageView = view.findViewById(R.id.search_list_icon)
		
		/**
		 * The name of the page
		 */
		val pagename: TextView = view.findViewById(R.id.search_list_pagename)
		
		/**
		 * The search result rank
		 */
		val rank: TextView = view.findViewById(R.id.search_list_rank)
		
		/**
		 * The (highlighted) search result context
		 */
		val context: TextView = view.findViewById(R.id.search_list_context)
		
		// TODO: Add the page tags here, but these are not returned by Pepperminty Wiki as of 2022-02-03 (that's also on the todo list)
	}
	
	/**
	 * Called when the RecyclerView wants to create a new item to display in the list
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemHolder {
		// Inflate a new instance of the view for this particular item
		val itemLayout = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_searchlist, parent, false)
		
		// Return a new Holder class instance
		return SearchResultItemHolder(itemLayout)
	}
	
	/**
	 * Fills a previously created view for a single item in the list with the data for a
	 * specific item in the dataset.
	 * @param holder: The holder class instance to fill with the data from the dataset.
	 * @param i: The index of the item in the dataset that is being requested.
	 */
	override fun onBindViewHolder(holder: SearchResultItemHolder, i: Int) {
		// 1: Find the item
		val item = dataset[i]
		
		// 2: Fill the holder with the item's data
		holder.pagename.text = item.pagename
		holder.rank.text = item.rank.toString()
		holder.context.text = item.context // TODO: Highlight query search terms in this
		
		// 3: Attach event to say that an item has been selected
		holder.itemView.setOnClickListener {
			itemSelected.emit(this, ItemSelectedEventArgs(dataset[i]))
		}
	}
	
	/**
	 * Returns the number of items in the dataset.
	 */
	override fun getItemCount(): Int {
		return dataset.size
	}
	
	
	/*****************
	 * Filter system *
	 *****************/
	private val filter: Filter = object : Filter() {
		override fun performFiltering(constraint: CharSequence): FilterResults {
			val filterPattern = constraint.toString()
				.lowercase(Locale.getDefault())
				.trim()
			
			val filteredList = mutableListOf<WikiSearchResult>()
			if (filterPattern.isEmpty()) {
				filteredList.addAll(raw_dataset)
			} else {
				for (item in raw_dataset) {
					// TODO: Search tags here too, but we don't current have them to hand
					if (item.pagename.lowercase(Locale.getDefault()).contains(filterPattern)
						|| item.context.lowercase(Locale.getDefault()).contains(filterPattern))
						filteredList.add(item)
				}
			}
			val results = FilterResults()
			results.values = filteredList
			return results
		}
		
		override fun publishResults(constraint: CharSequence, results: FilterResults) {
			val values : List<WikiSearchResult> = (results.values as List<*>).filterIsInstance<WikiSearchResult>()
			dataset.clear()
			dataset.addAll(values)
			notifyDataSetChanged()
		}
	}
	
	override fun getFilter(): Filter {
		return filter
	}
}