package com.sbrl.peppermint.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.display.PageListAdapter
import com.sbrl.peppermint.display.WikiPageInfo


/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class WikiPageList : Fragment() {
	private val LogTag = "WikiPageList"
	
	private lateinit var containingView : View
	
	private var interactionListener : OnListFragmentInteractionListener? = null
	
	private lateinit var swipeDetector : SwipeRefreshLayout
	
	private lateinit var pageListAdapter : PageListAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pageListAdapter = PageListAdapter(arrayListOf(), context!!)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		containingView = inflater.inflate(R.layout.fragment_page_list, container, false)
		
		
		swipeDetector = containingView.findViewById(R.id.page_list_refresh_detector)
		
		attachRefreshListener(swipeDetector)
		attachFilterQueryUpdateListeners(containingView.findViewById<SearchView>(R.id.page_list_filter))
		
		DisplayEmpty()
		
		return containingView
	}
	
	override fun onAttach(context: Context?) {
		super.onAttach(context)
		if (context is OnListFragmentInteractionListener)
			interactionListener = context
		else
			throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
	}
	
	override fun onDetach() {
		super.onDetach()
		interactionListener = null
	}
	
	private fun attachFilterQueryUpdateListeners(target : SearchView) {
		target.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextChange(query : String?): Boolean {
				pageListAdapter.filter.filter(query)
				return true
			}
			
			override fun onQueryTextSubmit(query : String?): Boolean {
				pageListAdapter.filter.filter(query)
				return true
			}
			
		})
	}
	
	private fun attachRefreshListener(target : SwipeRefreshLayout) {
		target.setOnRefreshListener {
			Log.i(LogTag, "Refresh requested via swipe gesture")
			
			interactionListener!!.onRefreshRequest()
		}
	}
	
	public fun PopulatePageList(rawPageList : List<String>, loadingComplete : Boolean) {
		val pageListDisplay : ListView = containingView.findViewById(R.id.page_list_main)
		
		val pageList : ArrayList<WikiPageInfo> = arrayListOf()
		for(nextPageName : String in rawPageList)
			pageList.add(WikiPageInfo(nextPageName, false))
		
		pageListAdapter = PageListAdapter(pageList, context!!)
		pageListDisplay.adapter = pageListAdapter
		
		pageListDisplay.onItemClickListener = AdapterView.OnItemClickListener {
				adapterView, view, position, id ->
			interactionListener?.onPageSelection(pageListAdapter.getItem(position))
		}
		
		// ---------
		
		val nothingHereMessage : TextView = containingView.findViewById(R.id.page_list_nothing_here)
		nothingHereMessage.visibility = View.GONE
		
		if(loadingComplete) {
			ToggleProgressDisplay(false)
			notify_send(context!!, getString(R.string.page_list_refreshed_list))
		}
	}
	
	public fun DisplayEmpty() {
		val nothingHereMessage : TextView = containingView.findViewById(R.id.page_list_nothing_here)
		nothingHereMessage.visibility = View.VISIBLE
		
		ToggleProgressDisplay(false)
	}
	
	public fun ToggleProgressDisplay(visible : Boolean) {
		swipeDetector.isRefreshing = visible
	}
	
	/* ********************************************************************** */
	
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 *
	 *
	 * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
	 */
	interface OnListFragmentInteractionListener {
		fun onRefreshRequest()
		
		fun onPageSelection(item: WikiPageInfo)
	}
}
