package com.sbrl.peppermint.fragments

import android.content.Intent
import android.os.Bundle
import com.sbrl.peppermint.R
import com.sbrl.peppermint.activities.TemplateNavigation
import com.sbrl.peppermint.activities.ViewPage
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.data.ConnectionStatus
import com.sbrl.peppermint.data.RecentChange
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.data.WikiManager
import kotlin.concurrent.thread

class RecentChanges() : TemplateNavigation(), RecentChangesList.OnListFragmentInteractionListener {
	private val LogTag = "[fragment] RecentChanges"
	
	lateinit var recentChangesFragment : RecentChangesList
	
	lateinit var currentWiki : Wiki
	
	protected override val contentId = R.layout.activity_recent_changes
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_recent_changes)
		
		recentChangesFragment = supportFragmentManager.findFragmentById(R.id.frag_main_content) as RecentChangesList
		
		thread(start = true) { wikiManager.setWiki("") }
	}
	
	override fun onWikiChangePre(wiki_name: String) {
		runOnUiThread {
			toolbar.title = wiki_name
			
			// Blank the recent changes list
			recentChangesFragment.PopulateRecentChangesList(
				arrayListOf(),
				false
			)
			
			masterView.closeDrawers()
			setSelectedWiki(wiki_name)
		}
	}
	
	override fun onWikiChangePost(wiki_name: String) {
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
		
		runOnUiThread {
			recentChangesFragment.PopulateRecentChangesList(changeList, true)
		}
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
