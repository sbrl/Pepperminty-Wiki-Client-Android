package com.sbrl.peppermint.ui.adapters

import android.content.Context
import android.util.Log
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
import com.sbrl.peppermint.lib.polyfill.human_filesize
import com.sbrl.peppermint.lib.polyfill.human_time_since
import com.sbrl.peppermint.lib.wiki_api.WikiRecentChange
import java.util.*

/**
 * Mediates between a dataset and a list of items being displayed on the screen through a list of
 * views by way of a RecyclerView.
 * In this particular instance, we are displaying a list of page names.
 * @source https://developer.android.com/codelabs/basic-android-kotlin-training-recyclerview-scrollable-list?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-2-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-recyclerview-scrollable-list#3
 */
class RecentChangesListAdapter (
	private val context: Context,
	private val raw_dataset: List<WikiRecentChange>
	) : RecyclerView.Adapter<RecentChangesListAdapter.RecentChangeItemHolder>(), Filterable {
	
	private val dataset = mutableListOf<WikiRecentChange>()
	
	init {
		dataset.addAll(raw_dataset)
	}
	
	class ItemSelectedEventArgs(val recentChange: WikiRecentChange)
	
	val itemSelected: EventManager<RecentChangesListAdapter, ItemSelectedEventArgs> = EventManager("PageListAdapter:itemSelected")
	
	/**
	 * Holds information about a single item that is being displayed in the list.
	 * RecyclerViews don't interact directly with the items in the list - preferring a
	 * helper class instance instead.
	 */
	class RecentChangeItemHolder(/*private val*/ view: View) : RecyclerView.ViewHolder(view) {
		/**
		 * The icon next to the recent change.
		 */
		val viewIcon: ImageView = view.findViewById(R.id.recent_change_item_icon)
		/**
		 * The text box that holds the name of the page.
		 */
		val viewPageName: TextView = view.findViewById(R.id.recent_change_item_page_name)
		/**
		 * The text box that holds the datetime the change was made.
		 */
		val viewDateTime: TextView = view.findViewById(R.id.recent_change_item_datetime)
		/**
		 * The text box that holds additional details about the change.
		 */
		val viewDetails: TextView = view.findViewById(R.id.recent_change_item_details)
		/**
		 * The text box that holds the name of the user who made the change.
		 */
		val viewUsername: TextView = view.findViewById(R.id.recent_change_user)
	}
	
	/**
	 * Called when the RecyclerView wants to create a new item to display in the list
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChangeItemHolder {
		// Inflate a new instance of the view for this particular item
		val itemLayout = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_recentchangeslist, parent, false)
		
		// Return a new Holder class instance
		return RecentChangeItemHolder(itemLayout)
	}
	
	/**
	 * Fills a previously created view for a single item in the list with the data for a
	 * specific item in the dataset.
	 * @param holder: The holder class instance to fill with the data from the dataset.
	 * @param i: The index of the item in the dataset that is being requested.
	 */
	override fun onBindViewHolder(holder: RecentChangeItemHolder, i: Int) {
		val item = dataset[i] // Find the item
		
		// Fill the holder with the item's data
		holder.viewPageName.text = item.pageName
		holder.viewUsername.text = context.getString(R.string.recent_change_user_by)
			.replace("{0}", item.user)
		holder.viewDateTime.text = context.getString(R.string.human_time_since_ago)
			.replace("{0}", human_time_since(item.datetime))
		
		holder.viewIcon.setImageResource(
			when(item.type.name.lowercase()) {
				"edit" -> R.drawable.icon_edit
				"upload" -> R.drawable.icon_upload
				"move" -> R.drawable.icon_move
				"deletion" -> R.drawable.icon_delete
				"comment" -> R.drawable.icon_comment
				else -> R.drawable.icon_unknown
			}
		)
		
		if(item.type.name.lowercase(Locale.getDefault()) == "edit" && item.payloadEdit()?.isNewPage == true) 
			holder.viewIcon.setImageResource(R.drawable.icon_add)
		
		holder.viewDetails.setTextColor(context.getColor(R.color.black_soft))
		holder.viewDetails.text = when(item.type.name.lowercase()) {
			"edit" -> {
				val sizeDiff = item.payloadEdit()?.sizeDiff ?: 0
				holder.viewDetails.setTextColor(context.getColor(
					when {
						sizeDiff > 0 -> R.color.colorOk
						sizeDiff < 0 -> R.color.colorError
						else -> R.color.black_soft
					}
				))
				"${if(sizeDiff >= 0) "+" else ""}${sizeDiff}"
			}
			"upload" -> {
				human_filesize(item.payloadUpload()?.fileSize?.toLong() ?: 0)
			}
			"move" -> {
				context.getString(R.string.recent_change_move_details)
					.replace("{0}", item.payloadMove()?.oldPageName ?: "")
			}
			"comment" -> {
				"${item.payloadComment()?.commentId ?: "(unknown)"} @ ${item.payloadComment()?.depth ?: ""}"
			}
			else -> ""
		}
		
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
			
			val filteredList = mutableListOf<WikiRecentChange>()
			if (filterPattern.isEmpty()) {
				filteredList.addAll(raw_dataset)
			} else {
				for (item in raw_dataset) {
					if (item.pageName.lowercase(Locale.getDefault()).contains(filterPattern)
						|| item.user.lowercase().contains(filterPattern))
						filteredList.add(item)
				}
			}
			val results = FilterResults()
			results.values = filteredList
			return results
		}
		
		override fun publishResults(constraint: CharSequence, results: FilterResults) {
			val values : List<WikiRecentChange> = (results.values as List<*>).filterIsInstance<WikiRecentChange>()
			dataset.clear()
			dataset.addAll(values)
			notifyDataSetChanged()
		}
	}
	
	override fun getFilter(): Filter {
		return filter
	}
}