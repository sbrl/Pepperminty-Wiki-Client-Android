package com.sbrl.peppermint.fragments

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.app.Fragment
import android.content.SharedPreferences
import android.preference.ListPreference
import com.sbrl.peppermint.R

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
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		addPreferencesFromResource(R.xml.preferences)
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
