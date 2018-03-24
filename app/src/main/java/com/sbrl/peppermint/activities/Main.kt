package com.sbrl.peppermint.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import kotlinx.android.synthetic.main.activity_main.view.*

class Main : AppCompatActivity() {
	private val LogTag = "[activity] Main"
	
    private lateinit var prefs : PreferencesManager
	
	private lateinit var masterView : DrawerLayout
	private lateinit var navigationDrawer : NavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
		
		prefs = PreferencesManager(this)
		
		masterView = findViewById(R.id.main_area)
		navigationDrawer = findViewById(R.id.main_drawer)
		
		// Setup the toolbar
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_nav_main)
		
		// Setup the event listeners
		navigationDrawer.setNavigationItemSelectedListener { onNavigationSelection(it) }
    }
	
	
	public override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when(item.itemId) {
			android.R.id.home -> {
				masterView.openDrawer(GravityCompat.START)
				Log.i(LogTag, "Opening navigation drawer")
				true // Return value
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
	
	private fun onNavigationSelection(selectedItem : MenuItem) : Boolean {
		Log.i(LogTag, "Navigation selection made")
		
		selectedItem.isChecked = true
		
		return when(selectedItem.itemId) {
			R.id.nav_main_add_wiki -> {
				val addWikiIntent = Intent(this, AddWiki::class.java)
				startActivity(addWikiIntent)
				
				true
			}
			else -> super.onOptionsItemSelected(selectedItem)
		}
	}
}
