package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.data.ConnectionStatus
import com.sbrl.peppermint.data.RecentChange
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.display.WikiPageInfo
import com.sbrl.peppermint.fragments.RecentChangesList
import com.sbrl.peppermint.fragments.WikiPageList
import kotlin.concurrent.thread

class RecentChanges() : TemplateNavigation(), RecentChangesList.OnListFragmentInteractionListener {
	private val LogTag = "[activity] RecentChanges"
	
	lateinit var recentChangesFragment : RecentChangesList
	
	lateinit var currentWiki : Wiki
	
	protected override val contentId = R.layout.activity_recent_changes
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_recent_changes)
		
		recentChangesFragment = supportFragmentManager.findFragmentById(R.id.frag_page_list) as RecentChangesList
		
		if(prefs.GetWikiList().size == 0) {
			recentChangesFragment.DisplayEmpty()
		} else {
			thread(start = true) { changeWiki("") }
		}
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
				notify_send(this, getString(R.string.nav_unknown_wiki).replace("{0}", newWikiName))
			}
			return
		}
		
		// We're clear to go ahead and switch to it - we think :P
		runOnUiThread {
			toolbar.title = newWikiName
			
			recentChangesFragment.PopulateRecentChangesList(arrayListOf(), false) // Blank the page list
			masterView.closeDrawers()
			setSelectedWiki(newWikiName)
		}
		
		val wikiData = prefs.GetCredentials(newWikiName)
		currentWiki = Wiki(this, newWikiName, wikiData)
		
		updateChangesList(false)
	}
	
	private fun updateChangesList(refreshFromInternet: Boolean) {
		
		val wikiStatus = currentWiki.TestConnection()
		val changeList: List<RecentChange> = if (wikiStatus == ConnectionStatus.Ok)
			currentWiki.GetRecentChanges(refreshFromInternet)
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
		
		runOnUiThread({
			recentChangesFragment.PopulateRecentChangesList(changeList, true)
		})
	}
	
	/* ****************************************************** */
	
	override fun onRefreshRequest() {
		thread(start = true) {
			updateChangesList(true)
		}
	}
	
	override fun onChangeSelection(item: RecentChange) {
		val intent = Intent(this, ViewPage::class.java)
		intent.putExtra("wiki-name", currentWiki.Name)
		intent.putExtra("page-name", item.PageName)
		startActivity(intent)
	}
	
}
