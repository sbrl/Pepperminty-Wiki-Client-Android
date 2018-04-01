package com.sbrl.peppermint.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.sbrl.peppermint.R
import com.sbrl.peppermint.display.PageListAdapter
import com.sbrl.peppermint.display.WikiPageInfo


/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class WikiPageList : Fragment() {
	private lateinit var containingView : View
	
	private var interactionListener : OnListFragmentInteractionListener? = null
	
	private lateinit var pageListAdapter : PageListAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pageListAdapter = PageListAdapter(arrayListOf(), context!!)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		containingView = inflater.inflate(R.layout.fragment_page_list, container, false)
		
		attachFilterQueryUpdateListeners(containingView.findViewById<SearchView>(R.id.page_list_filter))
		
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
	
	public fun PopulatePageList(rawPageList : List<String>) {
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
		
		toggleProgressDisplay(false)
	}
	
	public fun DisplayEmpty() {
		val nothingHereMessage : TextView = containingView.findViewById(R.id.page_list_nothing_here)
		nothingHereMessage.visibility = View.VISIBLE
		
		toggleProgressDisplay(false)
	}
	
	protected fun toggleProgressDisplay(visible : Boolean) {
		val progressDisplay : ProgressBar = containingView.findViewById(R.id.page_list_progress)
		progressDisplay.visibility = if(visible) View.VISIBLE else View.GONE
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
		fun onPageSelection(item: WikiPageInfo)
	}
}
