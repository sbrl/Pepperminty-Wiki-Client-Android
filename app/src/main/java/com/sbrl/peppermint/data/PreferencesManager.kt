package com.sbrl.peppermint.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

public class PreferencesManager {
	val LogTag : String = "PreferenceManager"
	private val sharedPrefsId = "com.sbrl.peppermint"
	
	private val credentialsPrefix = "wiki_"
	private val idWikiList = "wiki_list"
	
	private val prefsStorage: SharedPreferences
	
	public constructor(context : Context) {
		prefsStorage = context.getSharedPreferences(sharedPrefsId, MODE_PRIVATE)
	}
	
	fun HasCredentials(wikiName: String) : Boolean {
		return prefsStorage.contains(credentialsPrefix + wikiName)
	}
	
	fun GetCredentials(wikiName : String) : WikiCredentials {
		val rawCredentials = JSONObject(
				prefsStorage.getString(credentialsPrefix + wikiName, "")
		)
		return WikiCredentials(rawCredentials)
	}
	
	fun SetCredentials(wikiName : String, credentials : WikiCredentials) {
		val editor = prefsStorage.edit()
		
		editor.putString(credentialsPrefix + wikiName, credentials.toJSON().toString())
		
		editor.apply()
	}
	
	fun GetWikiList() : MutableList<String> {
		val rawWikiList = JSONArray(prefsStorage.getString(idWikiList, "[]"))
		val result = mutableListOf<String>()
		
		
		for(i in 0..(rawWikiList.length() - 1))
			result.add(i, rawWikiList.getString(i))
		
		return result
	}
	private fun setWikiList(wikiList : MutableList<String>) {
		val jsonList = JSONArray()
		for(i in 0..(wikiList.size - 1))
			jsonList.put(i, wikiList[i])
		
		val editor = prefsStorage.edit()
		editor.putString(idWikiList, jsonList.toString())
		editor.apply()
	}
	
	fun AddWiki(wikiName : String, credentials: WikiCredentials) {
		val wikiList : MutableList<String> = GetWikiList()
		wikiList.add(wikiName)
		setWikiList(wikiList)
		SetCredentials(wikiName, credentials)
	}
}

