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
import com.sbrl.peppermint.data.RecentChange
import com.sbrl.peppermint.display.RecentChangesListAdapter


/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecentChangesList : Fragment() {
	private val LogTag = "RecentChangesList"
	
	private lateinit var containingView : View
	
	private var interactionListener : OnListFragmentInteractionListener? = null
	
	private lateinit var swipeDetector : SwipeRefreshLayout
	
	private lateinit var changeListAdapter : RecentChangesListAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		changeListAdapter = RecentChangesListAdapter(arrayListOf(), context!!)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		containingView = inflater.inflate(R.layout.fragment_recent_changes, container, false)
		
		
		swipeDetector = containingView.findViewById(R.id.recent_changes_refresh_detector)
		
		attachRefreshListener(swipeDetector)
		attachFilterQueryUpdateListeners(containingView.findViewById<SearchView>(R.id.recent_changes_filter))
		
		// Be empty by default
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
				changeListAdapter.filter.filter(query)
				return true
			}
			
			override fun onQueryTextSubmit(query : String?): Boolean {
				changeListAdapter.filter.filter(query)
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
	
	public fun PopulateRecentChangesList(rawChangesList : List<RecentChange>, loadingComplete : Boolean) {
		if(rawChangesList.isEmpty()) {
			DisplayEmpty()
			return
		}
		
		val changeListDisplay : ListView = containingView.findViewById(R.id.recent_changes_main)
		
		// Take a shallow copy of the list
		val recentChangeList : ArrayList<RecentChange> = arrayListOf()
		for(nextItem in rawChangesList)
			recentChangeList.add(nextItem)
		
		changeListAdapter = RecentChangesListAdapter(recentChangeList, context!!)
		changeListDisplay.adapter = changeListAdapter
		
		changeListDisplay.onItemClickListener = AdapterView.OnItemClickListener {
				adapterView, view, position, id ->
			interactionListener?.onChangeSelection(changeListAdapter.getItem(position))
		}
		
		// ---------
		
		val nothingHereMessage : TextView = containingView.findViewById(R.id.recent_changes_nothing_here)
		nothingHereMessage.visibility = View.GONE
		
		if(loadingComplete) {
			toggleProgressDisplay(false)
			notify_send(context!!, getString(R.string.recent_changes_refreshed_list))
		}
	}
	
	public fun DisplayEmpty() {
		val nothingHereMessage : TextView = containingView.findViewById(R.id.recent_changes_nothing_here)
		nothingHereMessage.visibility = View.VISIBLE
		
		toggleProgressDisplay(false)
	}
	
	private fun toggleProgressDisplay(visible : Boolean) {
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
		
		fun onChangeSelection(item: RecentChange)
	}
}
