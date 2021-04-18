package com.sbrl.peppermint.ui

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.drawers.MainDrawerManager

class MainActivity : AppCompatActivity() {
    
    lateinit var drawerManager: MainDrawerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navBottom: BottomNavigationView = findViewById(R.id.nav_view)
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        val navDrawer: NavigationView = findViewById(R.id.navdraw_main)
        
        // TODO: Initialise drawerManager

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
}