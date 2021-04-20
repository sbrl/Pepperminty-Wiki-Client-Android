package com.sbrl.peppermint.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.io.SettingsManager

class SettingsActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
		if (savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.settings, SettingsFragment())
				.commit()
		}
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		
		SettingsManager(this).listAll()
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Settings appear to be saved automatically, apparently
		
		finish()
		
		super.onOptionsItemSelected(item)
		return true // Allow normal processing to continue
	}
	
	override fun onNavigateUp(): Boolean {
//		finish()
		return true
	}
	
	class SettingsFragment : PreferenceFragmentCompat() {
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
		}
	}
}