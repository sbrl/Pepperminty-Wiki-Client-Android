package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.data.ConnectionStatus
import com.sbrl.peppermint.data.RecentChange
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.display.WikiPageInfo
import com.sbrl.peppermint.fragments.RecentChangesList
import com.sbrl.peppermint.fragments.WikiPageList
import kotlin.concurrent.thread

class Main : TemplateNavigation(), WikiPageList.OnListFragmentInteractionListener, RecentChangesList.OnListFragmentInteractionListener {
	
	private val _logTag = "[activity] Main"
	
	
	private lateinit var fragmentPageList : WikiPageList
	private lateinit var fragmentRecentChanges : RecentChangesList
	
	private lateinit var contentFragmentContainer : FrameLayout
	private lateinit var toolbarBottom : BottomNavigationView
	
	private var currentWiki : Wiki? = null
 
	protected override val contentId = R.layout.activity_main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		// ~~~~~
		
		contentFragmentContainer = findViewById(R.id.frag_main_content)
		toolbarBottom = findViewById<BottomNavigationView>(R.id.toolbar_bottom)
		
		// ~~~~~
		
		fragmentPageList = WikiPageList()
		fragmentRecentChanges = RecentChangesList()
		
		setContentFragment(fragmentPageList)
		
		toolbarBottom.setOnNavigationItemSelectedListener(this::onBottomNavClick)
	
		// ~~~~~
		
		if(prefs.GetWikiList().size >= 0) {
			thread(start = true) { changeWiki("") }
		} else {
//			fragmentPageList.DisplayEmpty()
		}
    }
	
	@Suppress("RedundantOverride")
	override fun onResume() {
		super.onResume()
		
	}
	
	override fun changeWiki(wikiName: String) {
		lateinit var newWikiName : String
		if(wikiName.isNotEmpty()) {
			newWikiName = wikiName
		} else {
			newWikiName = if (intent.hasExtra(INTENT_WIKI_NAME))
				intent.getStringExtra(INTENT_WIKI_NAME)
			else ""
			
			if (newWikiName.isEmpty() && prefs.GetWikiList().size > 0)
				newWikiName = prefs.GetWikiList()[0]
		}
		
		// Don't change wiki if there aren't any wikis registered and we haven't been
		// asked to switch to a specific wiki
		if(newWikiName.isEmpty())
			return
		
		if(!prefs.HasCredentials(newWikiName)) {
			runOnUiThread {
				notify_send(this, "Unknown wiki name $newWikiName.")
			}
			return
		}
		
		// We're clear to go ahead and switch to it - we think :P
		runOnUiThread {
			toolbar.title = newWikiName
			
			// Blank the lists
			if(fragmentPageList.isAdded)
				fragmentPageList.PopulatePageList(arrayListOf(), false)
			else
				fragmentRecentChanges.PopulateRecentChangesList(arrayListOf(), false)
			
			masterView.closeDrawers()
			setSelectedWiki(newWikiName)
		}
		
		val wikiData = prefs.GetCredentials(newWikiName)
		currentWiki = Wiki(this, newWikiName, wikiData)
		
		updateDisplayFragment(false)
	}
	
	private fun updateDisplayFragment(refreshFromInternet: Boolean) {
		if(fragmentPageList.isAdded) {
			Log.i(this::class.java.name, "[smart updater] Updating page list")
			updatePageList(refreshFromInternet)
		}
		else {
			Log.i(this::class.java.name, "[smart updater] Updating recent changes")
			updateChangesList(refreshFromInternet)
		}
	}
	
	private fun updatePageList(refreshFromInternet : Boolean) {
		val wikiStatus = currentWiki!!.TestConnection()
		val pageList: List<String> = if (wikiStatus == ConnectionStatus.Ok)
			currentWiki!!.GetPageList(refreshFromInternet)
		else {
			runOnUiThread {
				notify_send(
					this,
					"Unable to connect to wiki (status $wikiStatus)"
				)
			}
			arrayListOf<String>() // Return value
		}
		
		runOnUiThread {
			fragmentPageList.PopulatePageList(pageList, true)
			if(refreshFromInternet) {
				notify_send(applicationContext!!, getString(R.string.page_list_refreshed_list))
			}
		}
	}
	
	private fun updateChangesList(refreshFromInternet: Boolean) {
		
		val wikiStatus = currentWiki!!.TestConnection()
		val changeList: List<RecentChange> = if (wikiStatus == ConnectionStatus.Ok)
			currentWiki!!.GetRecentChanges(refreshFromInternet)
		else {
			runOnUiThread {
				notify_send(
					this,
					getString(R.string.nav_connection_failed)
						.replace("{0}", wikiStatus.toString())
				)
			}
			arrayListOf<RecentChange>() // Return value
		}
		
		runOnUiThread {
			fragmentRecentChanges.PopulateRecentChangesList(changeList, true)
			if(refreshFromInternet)
				notify_send(applicationContext!!, getString(R.string.recent_changes_refreshed_list))
		}
	}
	
	private fun setContentFragment(fragment : Fragment) {
		supportFragmentManager.beginTransaction()
			.replace(R.id.main_container, fragment)
			.addToBackStack(null)
			.commit()
	}
	
	private fun navigatePage(wikiName : String, pageName : String) {
		val intent = Intent(this, ViewPage::class.java)
		intent.putExtra("wiki-name", wikiName)
		intent.putExtra("page-name", pageName)
		startActivity(intent)
		
	}
	
	/* ********************************************************************** */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_nav_top, menu)
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem) : Boolean = when(item.itemId) {
		R.id.main_menu_refresh -> {
			Log.i(_logTag, "Refresh requested via button")
			fragmentPageList.ToggleProgressDisplay(true)
			onRefreshRequest()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}
	
	private fun onBottomNavClick(selectedItem : MenuItem) : Boolean {
		when(selectedItem.itemId) {
			R.id.nav_main_bottom_page_list -> {
				setContentFragment(fragmentPageList)
				thread(start = true) { updatePageList(false) }
			}
			R.id.nav_main_bottom_recent_changes -> {
				setContentFragment(fragmentRecentChanges)
				thread(start = true) { updateChangesList(false) }
			}
		}
		
		return true
	}
	
	/* ********************************************************************** */
	
	
	override fun onRefreshRequest() {
		thread(start = true) {
			updateDisplayFragment(true)
		}
	}
	
	override fun onPageSelection(item: WikiPageInfo) {
		navigatePage(currentWiki!!.Name, item.Name)
	}
	
	override fun onChangeSelection(item: RecentChange) {
		// FUTURE: Add the ability to specify the specific page revision here
		navigatePage(currentWiki!!.Name, item.PageName)
	}
}
