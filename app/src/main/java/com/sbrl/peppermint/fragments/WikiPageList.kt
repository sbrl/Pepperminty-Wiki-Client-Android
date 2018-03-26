package com.sbrl.peppermint.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.sbrl.peppermint.R
import com.sbrl.peppermint.display.WikiPageInfo
import android.widget.AdapterView



/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class WikiPageList : Fragment() {
	
	private var interactionListener : OnListFragmentInteractionListener? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_page_list, container, false)
		
		// Setup the adapter
		
		// TODO Fill this in
		
		return view
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
	
	public fun PopulatePageList(rawPageList : List<String>) {
		val pageListDisplay : ListView = view!!.findViewById(R.id.page_list_main)
		
		val pageList : ArrayList<WikiPageInfo> = arrayListOf<WikiPageInfo>()
		for(nextPageName : String in rawPageList)
			pageList.add(WikiPageInfo(nextPageName, false))
		
		val adapter = PageListAdapter(pageList, context!!)
		pageListDisplay.adapter = adapter
		
		pageListDisplay.onItemClickListener = AdapterView.OnItemClickListener {
				adapterView, view, position, id ->
			interactionListener?.onPageSelection(pageList[position])
		}
	}
	
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
