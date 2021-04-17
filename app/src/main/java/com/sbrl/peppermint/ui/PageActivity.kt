package com.sbrl.peppermint.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sbrl.peppermint.R

class PageActivity : AppCompatActivity() {
    private lateinit var wikiViewModel: WikiViewModel
    private lateinit var pageViewModel: PageViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
	    // 0: Preamble
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pageview)
        
	    // 1: Navigation view
	    val navView: BottomNavigationView = findViewById(R.id.nav_view_page)
	    
        val navController = findNavController(R.id.nav_host_fragment_page)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_pageview, R.id.navigation_edit, R.id.navigation_history
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
		
	    
	    // 2: View models
	    pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java)
	    wikiViewModel = ViewModelProvider(this).get(WikiViewModel::class.java)
	    
	    wikiViewModel.init(this)
	    
	    // 3: Intents
	    
	    // Make sure a wiki and page name were specified
	    if(!intent.hasExtra(EXTRA_WIKI_NAME)) {
	    	Toast.makeText(this,
			    getString(R.string.error_no_wiki_name_specified),
			    Toast.LENGTH_SHORT).show()
		    finish()
	    }
	    if(!intent.hasExtra(EXTRA_PAGE_NAME)) {
		    Toast.makeText(this,
			    getString(R.string.error_no_page_name_specified),
			    Toast.LENGTH_SHORT).show()
		    finish()
	    }
	    
	    val wikiname = intent.getStringExtra(EXTRA_WIKI_NAME)!!
	    val pagename = intent.getStringExtra(EXTRA_PAGE_NAME)!!
	    Log.i("PageActivity", "Received intent wiki $wikiname, pagename $pagename")
	    
	    wikiViewModel.wikiManager.value?.setWiki(wikiname)
	    pageViewModel.pushPage(pagename)
    }
}