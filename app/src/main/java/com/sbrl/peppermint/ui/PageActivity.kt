package com.sbrl.peppermint.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.show_toast

/**
 * Displays a single page.
 * INTENT EXTRA IDS
 * 
 * EXTRA_WIKI_ID
 * EXTRA_PAGE_NAME
 */
class PageActivity : AppCompatActivity() {
    private lateinit var wikiViewModel: WikiViewModel
    private lateinit var pageViewModel: PageViewModel
	
	private lateinit var coordinatorLayout: CoordinatorLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
	    // 0: Preamble
        super.onCreate(savedInstanceState)
	    // Make the action bar be a thing - ref https://stackoverflow.com/a/10031400/1460422
	    window.requestFeature(Window.FEATURE_ACTION_BAR)
	    // Fill out the activity with content
        setContentView(R.layout.activity_pageview)
		
		coordinatorLayout = findViewById(R.id.coordinator_pageview)
		
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
	
		// Display a back button in the top left
	    supportActionBar?.setDisplayHomeAsUpEnabled(true)
	    
	    // 2: View models
	    pageViewModel = ViewModelProvider(this)[PageViewModel::class.java]
	    wikiViewModel = ViewModelProvider(this)[WikiViewModel::class.java]
	    
	    wikiViewModel.init(this)
	    
	    // 3: Intents
	    
	    // Make sure a wiki and page name were specified
	    if(!intent.hasExtra(EXTRA_WIKI_ID)) {
	    	show_toast(this, getString(R.string.error_no_wiki_name_specified))
		    finish()
	    }
	    if(!intent.hasExtra(EXTRA_PAGE_NAME)) {
			show_toast(this, getString(R.string.error_no_page_name_specified))
			finish()
	    }
	    
	    val wikiId = intent.getStringExtra(EXTRA_WIKI_ID)!!
	    val pagename = intent.getStringExtra(EXTRA_PAGE_NAME)!!
	    Log.i("PageActivity", "Received intent wiki $wikiId, pagename $pagename")
	    
	    wikiViewModel.wikiManager.value?.setWiki(wikiId)
	    pageViewModel.pushPage(pagename)
    }
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// HACK: Blindly assume it was the back button in the top left pressed here
		// I can't figure out a way to determine which button was actually pressed without replacing the entire bar with our own custom one.... sigh
		
		// If there isn't left on the stack after we've tried to pop something off, then exit this activity
		if(!pageViewModel.popPage() || pageViewModel.pageStackSize() == 0) {
			Log.i("PageActivity", "Nothing left on the stack, exiting")
			finish()
			return true
		}
		
		super.onOptionsItemSelected(item)
		return true // Allow normal processing to continue
	}
	
	
	override fun onBackPressed() {
		// If there isn't left on the stack after we've tried to pop something off, then we need to let the system take over the back button operation
		if(!pageViewModel.popPage() || pageViewModel.pageStackSize() == 0) {
			Log.i("PageActivity", "Nothing left on the stack, allowing system back operation")
			super.onBackPressed()
			return
		}
		
		Log.i("PageActivity", "Page stack pop success, intercepting back button operation")
	}
	
	fun showMessage(message: String) {
		show_toast(this, message)
		// SnackBars aren't working right at the moment, as they aren't always displaying :-/
//		Snackbar.make(coordinatorLayout, message, LENGTH_SHORT).show()
	}
}