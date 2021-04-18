package com.sbrl.peppermint.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.sbrl.peppermint.R

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
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// HACK: We just blindly open the drawer here, because we can't figure out an effective way to tellt he buttons on the action bar (that's the bar at the top of the screen) apart without replacing it with our own toolbar...... grumble
		
		// TODO: Save the settings here
		Log.w("SettingsActivity", "TODO: Save the settings here onOptionsItemSelected")
		
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