package com.sbrl.peppermint.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.emoji2.text.EmojiCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.send_add_wiki
import com.sbrl.peppermint.lib.ui.show_toast
import com.sbrl.peppermint.ui.drawers.MainDrawerManager


class MainActivity : AppCompatActivity() {
	
	lateinit var wikiViewModel: WikiViewModel
	lateinit var drawerManager: MainDrawerManager
	
	private lateinit var navBottom: BottomNavigationView
	lateinit var drawerLayout: DrawerLayout
	lateinit var navDrawer: NavigationView
	
	private lateinit var coordinatorLayout: CoordinatorLayout
	
	override fun onCreate(savedInstanceState: Bundle?) {
		// 1: Preamble
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		coordinatorLayout = findViewById(R.id.coordinator_main)
		
		// 2: View models
		wikiViewModel = ViewModelProvider(this)[WikiViewModel::class.java]
		wikiViewModel.init(this)
		
		if((wikiViewModel.wikiManager.value?.count() ?: 99) == 0) {
			send_add_wiki(this, clear_stack = true)
			return
		}
		
		// 3: Find references to views
		navBottom = findViewById(R.id.nav_view)
		drawerLayout = findViewById(R.id.container)
		navDrawer = findViewById(R.id.navdraw_main)
		
		drawerManager = MainDrawerManager(
			this,
			drawerLayout,
			navDrawer,
			wikiViewModel.wikiManager.value!!
		)
		
		// 4: Setup the action bar
		val navController = findNavController(R.id.nav_host_fragment)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_pagelist, R.id.navigation_recentchanges, R.id.navigation_search
			), drawerLayout
		)
		setupActionBarWithNavController(navController, appBarConfiguration)
		navBottom.setupWithNavController(navController)
		// Note to self: Do NOT call setupWithNavController here on the navDrawer - it will prevent us from listening to any events! Ref https://stackoverflow.com/a/62859704/1460422
	}
	
	/**
	 * Runs every time this activity resumes after another one has been displayed.
	 */
	override fun onResume() {
		wikiViewModel.wikiManager.value!!.reloadFromDisk()
		super.onResume()
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		Log.i("MainActivity", "onCreateOptionsMenu")
		menuInflater.inflate(R.menu.navdrawer_main, menu)
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// HACK: We just blindly open the drawer here, because we can't figure out an effective way to tell the buttons on the action bar (that's the bar at the top of the screen) apart without replacing it with our own toolbar...... grumble
		
		Log.i("MainActivity", "onOptionsItemSelected")
		
		drawerManager.toggleDrawer()
		super.onOptionsItemSelected(item)
		return true // Allow normal processing to continue
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		
		Log.i("MainActivity", "Reloading WikiManager from disk")
		wikiViewModel.wikiManager.value!!.reloadFromDisk()
		Log.i("MainActivity", "Current wiki is now ${wikiViewModel.wikiManager.value!!.currentWiki.name}")
		
		// Recreate the Main activity
		if(resultCode == RETURN_UPDATED_SETTINGS || resultCode == RETURN_ADDED_WIKI) {
			Log.i("MainActivity", "Got result code $resultCode, restarting activity")
			recreate()
		}
	}
	
	fun showSnackMessage(message: String) {
		show_toast(this, message)
		// SnackBars aren't working right at the moment, as they aren't always displaying :-/
//		Snackbar.make(coordinatorLayout, message, LENGTH_SHORT).show()
	}
}