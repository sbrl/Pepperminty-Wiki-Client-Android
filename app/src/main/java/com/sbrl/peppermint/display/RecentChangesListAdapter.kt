package com.sbrl.peppermint.display

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.sbrl.peppermint.R
import android.view.LayoutInflater
import android.widget.*
import com.sbrl.peppermint.data.*


class RecentChangesListAdapter : ArrayAdapter<RecentChange>, Filterable {
	private val items: List<RecentChange>
	private var itemsFiltered : List<RecentChange>
	private val appContext: Context
	
	constructor(
		inItems: List<RecentChange>,
		inContext: Context
	) : super(inContext, R.layout.fragment_page_list_item, inItems) {
		this.items = inItems
		this.itemsFiltered = this.items
		this.appContext = inContext
	}
	
	override fun getCount(): Int {
		return itemsFiltered.count()
	}
	override fun getItem(position: Int): RecentChange {
		return itemsFiltered[position]
	}
	override fun getItemId(position: Int): Long {
		return position.toLong()
	}
	
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		// 1. Create inflater
		val inflater = (context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
		
		// 2. Get rowView from inflater
		var rowView : View? = convertView
		if (rowView == null) // Inflate a new one if convertView was null
			rowView = inflater.inflate(R.layout.fragment_page_list_item, parent, false)
		
		populateDisplayItem(rowView!!, itemsFiltered[position])
		
		// 5. Return processed row view
		return rowView
	}
	
	private fun populateDisplayItem(row : View, recentChange: RecentChange) {
		// 3. Get sub-views from the row view
		// FUTURE: Use ViewHandler pattern here to speed things up if need be
		val iconDisplay : ImageView = row.findViewById(R.id.recent_change_item_icon)
		val pageNameDisplay : TextView = row.findViewById(R.id.recent_change_item_page_name)
		val dateDisplay : TextView = row.findViewById(R.id.recent_change_item_datetime)
		val detailsDisplay : TextView = row.findViewById(R.id.recent_change_details)
		val userDisplay : TextView = row.findViewById(R.id.recent_change_user)
		
		// 4. Update the fetched items from the row view
		iconDisplay.setImageResource(
			when(recentChange.Type) {
				"edit" -> R.drawable.icon_edit
				"upload" -> R.drawable.icon_upload
				"move" -> R.drawable.icon_move
				"deletion" -> R.drawable.icon_delete
				"comment" -> R.drawable.icon_comment
				else -> R.drawable.icon_unknown
			}
		)
		if(recentChange.Type == "edit" && (recentChange as RecentChangeEdit).NewPage)
			iconDisplay.setImageResource((R.drawable.icon_wiki_page))
		
		pageNameDisplay.text = recentChange.PageName
		dateDisplay.text = human_time_since(recentChange.Timestamp)
		detailsDisplay.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
		detailsDisplay.text = when(recentChange.Type) {
			"edit" -> {
				val editChange = recentChange as RecentChangeEdit
				detailsDisplay.setTextColor(ContextCompat.getColor(context,
					if(editChange.SizeDiff > 0) R.color.colorOk
					else if(editChange.SizeDiff < 0) R.color.colorError
					else R.color.colorTextSecondary))
				
				"${if(editChange.SizeDiff >= 0) "+" else ""}${editChange.SizeDiff}"
			}
			"upload" -> {
				val uploadChange = recentChange as RecentChangeUpload
				human_filesize(uploadChange.Filesize.toLong())
			}
			"move" -> {
				val moveChange = recentChange as RecentChangeMove
				context.getString(R.string.recent_change_move_details)
					.replace("{0}", moveChange.OldPageName)
			}
			"comment" -> {
				val commentChange = recentChange as RecentChangeComment
				"${commentChange.CommentId} @ ${commentChange.ReplyDepth}"
			}
			else -> ""
		}
		userDisplay.text = context.getString(R.string.recent_change_user_by)
			.replace("{0}", recentChange.User)
	}
	
	override fun getFilter(): Filter {
		return object : Filter() {
			override fun performFiltering(constraint: CharSequence?): FilterResults {
				val query : String = if(constraint == null || constraint.isEmpty())
					""
				else constraint.toString().trim().toLowerCase()
				
				val result = FilterResults()
				val resultList = arrayListOf<RecentChange>()
				for(changeData : RecentChange in items) {
					if(changeData.PageName.toLowerCase().contains(query) ||
						changeData.Type.toLowerCase().contains(query) ||
						changeData.User.toLowerCase().contains(query))
						resultList.add(changeData)
				}
				result.values = resultList
				
				return result
			}
			
			override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
				@Suppress("UNCHECKED_CAST")
				itemsFiltered = if(results == null)
					items
				else
					results.values as List<RecentChange>
				
				notifyDataSetChanged()
			}
			
		}
	}
	
}