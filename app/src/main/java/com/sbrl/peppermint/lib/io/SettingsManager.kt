package com.sbrl.peppermint.lib.io

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager

class SettingsManager(val context: Context) {
	private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
	
	fun listAll() {
		Log.i("SettingsManager", "*** Current Preferences ***")
		for((key, value) in prefs.all) {
			Log.i("SettingsManager", "$key = $value")
		}
		Log.i("SettingsManager", "***************************")
	}
	
	// The UI theme to use
	enum class Theme { UseSystem, Dark, Light }
	val theme: Theme
		get() = when(prefs.getString("theme", "use_system")) {
			"use_system" -> Theme.UseSystem
			"dark" -> Theme.Dark
			"light" -> Theme.Light
			else -> Theme.UseSystem
		}
	
	// When to load images
	enum class LoadImages { Always, OnlyOverWiFi, Never }
	val load_images: LoadImages
		get() = when(prefs.getString("load_images", "only_wifi")) {
			"always" -> LoadImages.Always
			"only_wifi" -> LoadImages.OnlyOverWiFi
			"never" -> LoadImages.Never
			else -> LoadImages.OnlyOverWiFi
		}
	
	// Whether offline mode is enabled
	val offline: Boolean
		get() = prefs.getBoolean("offlinemode", false)
}