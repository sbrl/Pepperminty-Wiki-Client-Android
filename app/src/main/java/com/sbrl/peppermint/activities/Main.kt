package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager

class Main : AppCompatActivity() {
	private val LogTag = "[activity] Main"
	
    private lateinit var prefs : PreferencesManager
	
	private lateinit var masterView : DrawerLayout
	private lateinit var navigationDrawer : NavigationView
	
	private var currentWiki : String = ""
	
    
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
	
	
	@Suppress("RedundantOverride")
	override fun onResume() {
		super.onResume()
		
		populateWikiList()
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
	
	private fun populateWikiList()
	{
		Log.i(LogTag, "Populating wiki list.")
		
		val wikiList : MutableList<String> = prefs.GetWikiList()
		
		navigationDrawer.menu.removeGroup(R.id.nav_main_wikis)
		
		for(wikiName : String in wikiList) {
			val newItem : MenuItem = navigationDrawer.menu.add(
				R.id.nav_main_wikis,
				Menu.NONE, Menu.FIRST,
				wikiName
			)
			newItem.icon = ContextCompat.getDrawable(this, R.drawable.icon_wiki)
		}
	}
}
