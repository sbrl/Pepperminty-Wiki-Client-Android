package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import kotlin.concurrent.thread

public abstract class TemplateNavigation : AppCompatActivity()
{
	
	protected lateinit var prefs : PreferencesManager
	
	protected lateinit var masterView : DrawerLayout
	protected lateinit var toolbar : Toolbar
	protected lateinit var navigationDrawer : NavigationView
	
	protected abstract val contentId : Int
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(contentId)
		
		// ---------------------------------------
		
		prefs = PreferencesManager(this)
		
		// ---------------------------------------
		// ---------------- Views ----------------
		// ---------------------------------------
		masterView = findViewById(R.id.main_area)
		toolbar = findViewById(R.id.toolbar)
		navigationDrawer = findViewById(R.id.main_drawer)
		
		// ---------------------------------------
		// --------------- Toolbar ---------------
		// ---------------------------------------
		
		// Setup the toolbar
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_nav_main)
		
		
		// ---------------------------------------
		// ----------- Event Listeners -----------
		// ---------------------------------------
		
		navigationDrawer.setNavigationItemSelectedListener { onNavigationSelection(it) }
		
	}
	
	public override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when(item.itemId) {
			android.R.id.home -> {
				masterView.openDrawer(GravityCompat.START)
				Log.i(this::class.java.name, "Opening navigation drawer")
				true // Return value
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
	
	private fun onNavigationSelection(selectedItem : MenuItem) : Boolean {
		Log.i(this::class.java.name, "Navigation selection made")
		
		selectedItem.isChecked = true
		
		// Handle wiki names
		if(selectedItem.groupId == R.id.nav_main_wikis) {
			thread(start = true) { changeWiki(selectedItem.title.toString()) }
			return true
		}
		
		return when(selectedItem.itemId) {
			R.id.nav_main_add_wiki -> {
				val addWikiIntent = Intent(this, AddWiki::class.java)
				startActivity(addWikiIntent)
				
				true
			}
			else -> super.onOptionsItemSelected(selectedItem)
		}
	}
	
	protected abstract fun changeWiki(wikiName: String)
}