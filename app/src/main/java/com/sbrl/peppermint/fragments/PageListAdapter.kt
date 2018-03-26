package com.sbrl.peppermint.fragments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.sbrl.peppermint.R
import com.sbrl.peppermint.display.WikiPageInfo
import android.widget.TextView
import android.view.LayoutInflater
import android.widget.ImageView


class PageListAdapter : ArrayAdapter<WikiPageInfo> {
	private val items: List<WikiPageInfo>
	private val appContext: Context
	
	constructor(
		inItems: List<WikiPageInfo>,
		inContext: Context
	) : super(inContext, R.layout.fragment_page_list_item, inItems) {
		this.items = inItems
		this.appContext = inContext
	}
	
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		// 1. Create inflater
		val inflater = (context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
		
		// 2. Get rowView from inflater
		var rowView : View? = convertView
		if (rowView == null) // Inflate a new one if convertView was null
			rowView = inflater.inflate(R.layout.fragment_page_list_item, parent, false)
		
		populateDisplayItem(rowView!!, items[position])
		
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
}
