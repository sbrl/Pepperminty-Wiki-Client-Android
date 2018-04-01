package com.sbrl.peppermint.display

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.sbrl.peppermint.R
import com.sbrl.peppermint.display.WikiPageInfo
import android.view.LayoutInflater
import android.widget.*


class PageListAdapter : ArrayAdapter<WikiPageInfo>, Filterable {
	private val items: List<WikiPageInfo>
	private var itemsFiltered : List<WikiPageInfo>
	private val appContext: Context
	
	constructor(
		inItems: List<WikiPageInfo>,
		inContext: Context
	) : super(inContext, R.layout.fragment_page_list_item, inItems) {
		this.items = inItems
		this.itemsFiltered = this.items
		this.appContext = inContext
	}
	
	override fun getCount(): Int {
		return itemsFiltered.count()
	}
	override fun getItem(position: Int): WikiPageInfo {
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
	
	private fun populateDisplayItem(row : View, pageInfo : WikiPageInfo) {
		// 3. Get sub-views from the row view
		// FUTURE: Use ViewHandler pattern here to speed things up if need be
		val iconDisplay : ImageView = row.findViewById(R.id.page_list_item_icon)
		val descriptionDisplay : TextView = row.findViewById(R.id.page_list_item_name)
		
		// 4. Update the fetched items from the row view
		iconDisplay.setImageResource(
			if(pageInfo.IsDownloaded) R.drawable.icon_downloaded_device
			else R.drawable.icon_wiki_page
		)
		descriptionDisplay.text = pageInfo.Name
	}
	
	override fun getFilter(): Filter {
		return object : Filter() {
			override fun performFiltering(constraint: CharSequence?): FilterResults {
				val query : String = if(constraint == null || constraint.isEmpty())
					""
				else constraint.toString().toLowerCase()
				
				val result = FilterResults()
				val resultList = arrayListOf<WikiPageInfo>()
				for(pageData : WikiPageInfo in items) {
					if(pageData.Name.toLowerCase().contains(query))
						resultList.add(pageData)
				}
				result.values = resultList
				
				return result
			}
			
			override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
				@Suppress("UNCHECKED_CAST")
				itemsFiltered = if(results == null)
					items
				else
					results.values as List<WikiPageInfo>
				
				notifyDataSetChanged()
			}
			
		}
	}
	
}