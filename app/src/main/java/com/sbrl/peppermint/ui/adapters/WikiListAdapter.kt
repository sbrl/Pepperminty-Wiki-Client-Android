package com.sbrl.peppermint.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.events.EventManager
import com.sbrl.peppermint.lib.wiki_api.ConnectionStatus
import com.sbrl.peppermint.lib.wiki_api.Wiki
import java.util.*
import kotlin.concurrent.thread

/**
 * Mediates between a dataset and a list of items being displayed on the screen through a list of
 * views by way of a RecyclerView.
 * In this particular instance, we are displaying a list of wikis.
 * @source https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#3
 */
class WikiListAdapter (
	private val context: Activity,
	private val raw_dataset: List<Wiki>
	) : RecyclerView.Adapter<WikiListAdapter.WikiItemHolder>(), Filterable {
	
	private val dataset = mutableListOf<Wiki>()
	
	init {
		dataset.addAll(raw_dataset)
	}
	
	class ItemSelectedRemoveEventArgs(val wiki: Wiki)
	
	val itemSelectedRemove: EventManager<WikiListAdapter, ItemSelectedRemoveEventArgs> = EventManager("WikiListAdapter:itemSelectedRemove")
	
	/**
	 * Holds information about a single item that is being displayed in the list.
	 * RecyclerViews don't interact directly with the items in the list - preferring a
	 * helper class instance instead.
	 */
	class WikiItemHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		/**
		 * The text box that holds the name of the wiki.
		 */
		val viewWikiName: TextView = view.findViewById(R.id.wiki_list_wikiname)
		
		/**
		 * The image that displays the connection status of the wiki.
		 */
		val viewWikiConnectionStatus: ImageView = view.findViewById(R.id.wiki_list_icon_connection_status)
		
		/**
		 * The text box that holds the endpoint/url of the wiki.
		 */
		val viewWikiEndpoint: TextView = view.findViewById(R.id.wiki_list_endpoint)
		
		/**
		 * The icon next to the name of the wiki.
		 * FUTURE: In the future, we might want to assign a different icon to different types of page.
		 */
		val viewIcon: ImageView = view.findViewById(R.id.wiki_list_icon)
		
		/**
		 * The remove button.
		 */
		val viewButtonRemove: ImageButton = view.findViewById(R.id.wiki_list_button_remove)
	}
	
	/**
	 * Called when the RecyclerView wants to create a new item to display in the list
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WikiItemHolder {
		// Inflate a new instance of the view for this particular item
		val itemLayout = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_wikilist, parent, false)
		
		// Return a new Holder class instance
		return WikiItemHolder(itemLayout)
	}
	
	
	/**
	 * Fills a previously created view for a single item in the list with the data for a
	 * specific item in the dataset.
	 * @param holder: The holder class instance to fill with the data from the dataset.
	 * @param i: The index of the item in the dataset that is being requested.
	 */
	override fun onBindViewHolder(holder: WikiItemHolder, i: Int) {
		val item = dataset[i] // Find the item
		
		// 1: Fill the holder with the item's data
		holder.viewWikiName.text = item.name
		holder.viewWikiEndpoint.text = item.endpoint
		
		
		thread {
			val connStatus = item.connectionStatus()
			
			context.runOnUiThread {
				holder.viewWikiConnectionStatus.setImageDrawable(context.getDrawable(
					when(connStatus) {
						ConnectionStatus.ConnectionFailed -> R.drawable.icon_cross
						ConnectionStatus.CredentialsIncorrect -> R.drawable.icon_cross
						ConnectionStatus.CredentialsRequired -> R.drawable.icon_cross
						ConnectionStatus.Ok -> R.drawable.icon_ok
						ConnectionStatus.Untested -> R.drawable.icon_unknown
						else -> R.drawable.icon_warning
					}
				))
				holder.viewWikiConnectionStatus.imageTintList = ColorStateList.valueOf(context.getColor(when(connStatus) {
					ConnectionStatus.CredentialsRequired -> R.color.colorError
					ConnectionStatus.CredentialsIncorrect -> R.color.colorError
					ConnectionStatus.ConnectionFailed -> R.color.colorError
					ConnectionStatus.Ok -> R.color.colorOk
					ConnectionStatus.Untested -> R.color.colorInfo
					else -> R.color.colorWarning
				}))
				
				// 2: Attach events
				holder.viewButtonRemove.setOnClickListener {
					itemSelectedRemove.emit(this, ItemSelectedRemoveEventArgs(dataset[i]))
				}
			}
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
			
			val filteredList = mutableListOf<Wiki>()
			if (filterPattern.isEmpty()) {
				filteredList.addAll(raw_dataset)
			} else {
				for (item in raw_dataset) {
					if (item.name.lowercase(Locale.getDefault()).contains(filterPattern))
						filteredList.add(item)
				}
			}
			val results = FilterResults()
			results.values = filteredList
			return results
		}
		
		override fun publishResults(constraint: CharSequence, results: FilterResults) {
			val values : List<Wiki> = (results.values as List<*>).filterIsInstance<Wiki>()
			dataset.clear()
			dataset.addAll(values)
			notifyDataSetChanged()
		}
	}
	
	override fun getFilter(): Filter {
		return filter
	}
}