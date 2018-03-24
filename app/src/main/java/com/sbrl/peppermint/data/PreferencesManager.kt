package com.sbrl.peppermint.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import org.json.JSONObject

public class PreferencesManager {
	val LogTag: String = "PreferenceManager"
	private val sharedPrefsId = "com.sbrl.peppermint"
	
	private val credentialsPrefix = "wiki_"
	
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
}

