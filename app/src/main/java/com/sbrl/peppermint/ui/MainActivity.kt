package com.sbrl.peppermint.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.drawers.MainDrawerManager

class MainActivity : AppCompatActivity() {
	
	lateinit var wikiViewModel: WikiViewModel
	lateinit var drawerManager: MainDrawerManager
	
	lateinit var navBottom: BottomNavigationView
	lateinit var drawerLayout: DrawerLayout
	lateinit var navDrawer: NavigationView
	
	override fun onCreate(savedInstanceState: Bundle?) {
		// 1: Preamble
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		// 2: View models
		wikiViewModel = ViewModelProvider(this).get(WikiViewModel::class.java)
		wikiViewModel.init(this)
		
		
		// 3: Find references to views
		navBottom = findViewById(R.id.nav_view)
		drawerLayout = findViewById(R.id.container)
		navDrawer = findViewById(R.id.navdraw_main)
		
		drawerManager = MainDrawerManager(this, navDrawer, wikiViewModel.wikiManager.value!!)
		
		
		// 4: Setup the action bar
		val navController = findNavController(R.id.nav_host_fragment)
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(setOf(
			R.id.navigation_pagelist, R.id.navigation_recentchanges, R.id.navigation_search
		), drawerLayout)
		setupActionBarWithNavController(navController, appBarConfiguration)
		navBottom.setupWithNavController(navController)
		navDrawer.setupWithNavController(navController)
		
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// HACK: We just blindly open the drawer here, because we can't figure out an effective way to tellt he buttons on the action bar (that's the bar at the top of the screen) apart without replacing it with our own toolbar...... grumble
		if(drawerLayout.isDrawerOpen(navDrawer))
			drawerLayout.closeDrawer(GravityCompat.START)
		else
			drawerLayout.openDrawer(GravityCompat.START)
		super.onOptionsItemSelected(item)
		return true // Allow normal processing to continue
	}
	
	override fun onNavigateUp(): Boolean {
		Log.i("MainActivity", "Opening drawer")
		drawerLayout.openDrawer(GravityCompat.START)
		return super.onNavigateUp()
	}
}