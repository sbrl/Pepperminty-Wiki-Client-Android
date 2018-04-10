package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.data.ConnectionStatus
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.display.WikiPageInfo
import com.sbrl.peppermint.fragments.WikiPageList
import kotlin.concurrent.thread

class Main : TemplateNavigation(), WikiPageList.OnListFragmentInteractionListener {
	
	private val LogTag = "[activity] Main"
	
	private val INTENT_WIKI_NAME = "wiki-name"
	
	private lateinit var pageListFragment : WikiPageList
	
	private var currentWiki : Wiki? = null
 
	protected override val contentId = R.layout.activity_main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		
		pageListFragment = supportFragmentManager.findFragmentById(R.id.frag_page_list) as WikiPageList
		
		if(prefs.GetWikiList().size == 0) {
			pageListFragment.DisplayEmpty()
		} else {
			thread(start = true) { changeWiki("") }
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
			
			pageListFragment.PopulatePageList(arrayListOf(), false) // Blank the page list
			masterView.closeDrawers()
			setSelectedWiki(newWikiName)
		}
		
		val wikiData = prefs.GetCredentials(newWikiName)
		currentWiki = Wiki(this, newWikiName, wikiData)
		
		updatePageList(false)
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
		
		runOnUiThread({
			pageListFragment.PopulatePageList(pageList, true)
		})
	}
	
	/* ********************************************************************** */
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_options, menu)
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem) : Boolean = when(item.itemId) {
		R.id.main_menu_refresh -> {
			Log.i(LogTag, "Refresh requested via button")
			pageListFragment.ToggleProgressDisplay(true)
			onRefreshRequest()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}
	
	/* ********************************************************************** */
	
	
	override fun onRefreshRequest() {
		thread(start = true) {
			updatePageList(true)
		}
	}
	
	override fun onPageSelection(item: WikiPageInfo) {
		val intent = Intent(this, ViewPage::class.java)
		intent.putExtra("wiki-name", currentWiki!!.Name)
		intent.putExtra("page-name", item.Name)
		startActivity(intent)
	}
}
