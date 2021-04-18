package com.sbrl.peppermint.ui.drawers

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.google.android.material.navigation.NavigationView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.wiki_api.WikiManager
import com.sbrl.peppermint.ui.EXTRA_WIKI_ID

class MainDrawerManager(val context: Context, val drawer: NavigationView, val wikiManager: WikiManager) {
	init {
		drawer.setNavigationItemSelectedListener(::handleItemSelected)
	}
	
	private fun handleItemSelected(it: MenuItem): Boolean {
		
		return true
	}
	
	/**
	 * Populates the list of wikis in the navigation drawer.
	 */
	private fun populateWikiList() {
		// TODO: Clear the group out first to ensure we don't double up
		for((id, wiki) in wikiManager.getWikiList()) {
			val nextMenuItem: MenuItem = drawer.menu.add(
				R.id.navdrawer_main_wikilist,
				Menu.NONE, Menu.FIRST,
				wiki.name
			)
			nextMenuItem.intent.putExtra(EXTRA_WIKI_ID, wiki.id)
			nextMenuItem.icon = ContextCompat.getDrawable(context, R.drawable.icon_wiki)
		}
	}
}