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
import com.sbrl.peppermint.data.WikiCredentials
import com.sbrl.peppermint.data.WikiManager
import kotlin.concurrent.thread

public abstract class TemplateNavigation : AppCompatActivity(), WikiManager.WikiManagerEventListener
{
	val INTENT_WIKI_NAME = "wiki-name"
	
	lateinit var prefs : PreferencesManager
	
	protected lateinit var masterView : DrawerLayout
	protected lateinit var toolbar : Toolbar
	protected lateinit var navigationDrawer : NavigationView
	
	protected lateinit var wikiManager: WikiManager
	
	
	protected abstract val contentId : Int
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(contentId)
		
		// ---------------------------------------
		
		prefs = PreferencesManager(this)
		wikiManager = WikiManager(this, this)
		
		// ---------------------------------------
		// ---------------- Views ----------------
		// ---------------------------------------
		masterView = findViewById(R.id.main_area)
		toolbar = findViewById(R.id.toolbar_top)
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
	
	override fun onResume() {
		super.onResume()
		
		populateWikiList()
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
	protected fun setSelectedWiki(wikiName : String) {
		
		var i = 0; var nextItem : MenuItem
		while(i < navigationDrawer.menu.size()) {
			nextItem = navigationDrawer.menu.getItem(i)
			
			Log.i(this::class.java.name, "setSelectedWiki: ${nextItem.title} / $wikiName")
			
			Log.i(this::class.java.name, "setSelectedWiki: Making ${nextItem.title} " + if(nextItem.title != wikiName) "selected" else "unselected")
			nextItem.isChecked = nextItem.title == wikiName
			
			i++
		}
		
	}
	private fun onNavigationSelection(selectedItem : MenuItem) : Boolean {
		Log.i(this::class.java.name, "Navigation selection made")
		
		
		// Handle wiki names
		if(selectedItem.groupId == R.id.nav_main_wikis) {
			selectedItem.isChecked = true
			thread(start = true) { wikiManager.setWiki(selectedItem.title.toString()) }
			return true
		}
		
		return when(selectedItem.itemId) {
			R.id.nav_main_add_wiki -> {
				val addWikiIntent = Intent(this, AddWiki::class.java)
				startActivity(addWikiIntent)
				
				true
			}
			R.id.nav_main_settings -> {
				val settingsIntent = Intent(this, Settings::class.java)
				startActivity(settingsIntent)
				true
			}
			R.id.nav_main_credits -> {
				val creditsIntent = Intent(this, ViewPage::class.java)
				creditsIntent.putExtra("page-name", "@@___credits")
				startActivity(creditsIntent)
				true
			}
			else -> super.onOptionsItemSelected(selectedItem)
		}
	}
	
	private fun populateWikiList()
	{
		Log.i(this::class.java.name, "Populating wiki list.")
		
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