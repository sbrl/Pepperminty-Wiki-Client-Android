package com.sbrl.peppermint.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Fragment
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.ListPreference
import android.preference.Preference
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.DataManager
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.display.human_filesize

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Preferences.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Preferences.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class Preferences : PreferenceFragment() {
	
	//private var listener: OnFragmentInteractionListener? = null
	
	private lateinit var cacheButton : Preference
	private lateinit var dataManager : DataManager
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		addPreferencesFromResource(R.xml.preferences)
		
		cacheButton = findPreference(getString(R.string.pref_clear_cache))
		cacheButton.setOnPreferenceClickListener {
			handlePrefCacheClearClick(it)
			false
		}
		dataManager = DataManager(cacheButton.context) // 'cause this.context requires API level 23+
		updateCacheSize()
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		/*if (context is OnFragmentInteractionListener) {
			listener = context
		} else {
			throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
		}*/
	}
	
	override fun onDetach() {
		super.onDetach()
		//listener = null
	}
	
	private fun updateCacheSize() {
		cacheButton.summary = getString(R.string.pref_cache_clear_current_size) + human_filesize(dataManager.CalculateCacheSize())
	}
	
	private fun handlePrefCacheClearClick(preference : Preference) {
		AlertDialog.Builder(preference.context)
			.setTitle(getString(R.string.pref_clear_cache))
			.setMessage(getString(R.string.pref_clear_cache_confirm))
			.setPositiveButton(android.R.string.yes, { _: DialogInterface, _: Int ->
				dataManager.ClearCache()
				updateCacheSize()
				notify_send(preference.context, getString(R.string.pref_clear_cache_complete))
			})
			.setNegativeButton(android.R.string.no, null).show()
	}
	
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 *
	 *
	 * See the Android Training lesson [Communicating with Other Fragments]
	 * (http://developer.android.com/training/basics/fragments/communicating.html)
	 * for more information.
	 */
	/*interface OnFragmentInteractionListener {
	
	}*/
}
