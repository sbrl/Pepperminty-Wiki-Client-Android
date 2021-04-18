package com.sbrl.peppermint.ui.drawers

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.wiki_api.WikiManager
import com.sbrl.peppermint.ui.EXTRA_WIKI_ID
import com.sbrl.peppermint.ui.PageViewModel
import com.sbrl.peppermint.ui.addwiki.AddWikiActivity
import com.sbrl.peppermint.ui.settings.SettingsActivity

class MainDrawerManager(val context: Context,
                        val drawerLayout: DrawerLayout,
                        val navDrawer: NavigationView,
                        val wikiManager: WikiManager) {
	init {
		navDrawer.setNavigationItemSelectedListener {
			Log.i("MainDrawerManager", "setNavigationItemSelectedListener")
			handleItemSelected(it)
		}
//		navDrawer.setNavigationItemSelectedListener(::handleItemSelected)
		navDrawer.bringToFront()
		populateWikiList()
	}
	
	/**
	 * Handle the selection of an item in the navigation drawer.
	 * Ref https://developer.android.com/reference/com/google/android/material/navigation/NavigationView.OnNavigationItemSelectedListener
	 * @param it: The item that was selected.
	 * @return Whether we were able to successfully handle it or not.
	 */
	private fun handleItemSelected(it: MenuItem) : Boolean {
		Log.i("MainDrawerManager", "Handling selected item")
		
		// Try to handle it as a wiki
		if(handleSelectWiki(it)) return true
		
		Log.i("MainDrawerManager", "It wasn't a wiki")
		
		// It's not a wiki - let's go through the special buttons
		return when(it.itemId) {
			R.id.navdrawer_main_add_wiki -> {
				val addWikiIntent = Intent(context, AddWikiActivity::class.java)
				context.startActivity(addWikiIntent)
				
				true
			}
			R.id.navdrawer_main_settings -> {
				val settingsIntent = Intent(context, SettingsActivity::class.java)
				context.startActivity(settingsIntent)
				true
			}
			R.id.navdrawer_main_credits -> {
				val creditsIntent = Intent(context, PageViewModel::class.java)
				context.startActivity(creditsIntent)
				true
			}
			else -> false // Don't display this as the active item, since if we haven't implemented any functionality yet it won't do anything
		}
	}
	
	/**
	 * Switches wiki based on the given menu item.
	 * @param it: The MenuItem to extra the wiki id from.
	 * @return Whether we were able to successfully switch wiki. If false, then we couldn't find a wiki id attached to the given menu item.
	 */
	private fun handleSelectWiki(it: MenuItem) : Boolean {
		if(it.intent == null || !it.intent.hasExtra(EXTRA_WIKI_ID)) return false
		
		
		val wikiId = it.intent.getStringExtra(EXTRA_WIKI_ID) ?: return false
		
		Log.i("MainDrawerManager", "Selecting wiki with id $wikiId and display text ${it.title}")
		wikiManager.setWiki(wikiId)
		return true
	}
	
	/**
	 * Toggles the open/closed state of this navigation drawer.
	 */
	fun toggleDrawer() {
		if(drawerLayout.isDrawerOpen(navDrawer))
			drawerLayout.closeDrawer(GravityCompat.START)
		else
			drawerLayout.openDrawer(GravityCompat.START)
	}
	
	/**
	 * Populates the list of wikis in the navigation drawer.
	 */
	private fun populateWikiList() {
		// TODO: Clear the group out first to ensure we don't double up
		for((id, wiki) in wikiManager.getWikiList()) {
			val nextMenuItem: MenuItem = navDrawer.menu.add(
				R.id.navdrawer_main_wikilist,
				Menu.NONE, Menu.FIRST,
				wiki.name
			)
			nextMenuItem.intent = Intent().apply {
				putExtra(EXTRA_WIKI_ID, wiki.id)
			}
			nextMenuItem.icon = ContextCompat.getDrawable(context, R.drawable.icon_wiki)
		}
	}
}