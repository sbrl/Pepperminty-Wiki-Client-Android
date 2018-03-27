package com.sbrl.peppermint.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.sbrl.peppermint.R
import com.sbrl.peppermint.bricks.notify_send
import com.sbrl.peppermint.data.ConnectionStatus
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.Wiki
import com.sbrl.peppermint.display.WikiPageInfo
import com.sbrl.peppermint.fragments.WikiPageList
import kotlin.concurrent.thread

class Main : AppCompatActivity(), WikiPageList.OnListFragmentInteractionListener {
	
	private val LogTag = "[activity] Main"
	
	private val INTENT_WIKI_NAME = "wiki-name"
	
    private lateinit var prefs : PreferencesManager
	
	private lateinit var masterView : DrawerLayout
	private lateinit var navigationDrawer : NavigationView
	
	private lateinit var pageListFragment : WikiPageList
	
	private var currentWiki : Wiki? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
		
		prefs = PreferencesManager(this)
		
		masterView = findViewById(R.id.main_area)
		navigationDrawer = findViewById(R.id.main_drawer)
		
		pageListFragment = supportFragmentManager.findFragmentById(R.id.frag_page_list) as WikiPageList
		// TODO: Register selection listener here
		
		// Setup the toolbar
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_nav_main)
		
		// Setup the event listeners
		navigationDrawer.setNavigationItemSelectedListener { onNavigationSelection(it) }
	
		thread(start = true) { changeWiki() }
    }
	
	@Suppress("RedundantOverride")
	override fun onResume() {
		super.onResume()
		
		populateWikiList()
	}
	
	public override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when(item.itemId) {
			android.R.id.home -> {
				masterView.openDrawer(GravityCompat.START)
				Log.i(LogTag, "Opening navigation drawer")
				true // Return value
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
	
	private fun onNavigationSelection(selectedItem : MenuItem) : Boolean {
		Log.i(LogTag, "Navigation selection made")
		
		selectedItem.isChecked = true
		
		// Handle wiki names
		if(selectedItem.groupId == R.id.nav_main_wikis) {
			thread(start = true) { changeWiki(selectedItem.title.toString()) }
			return true
		}
		
		return when(selectedItem.itemId) {
			R.id.nav_main_add_wiki -> {
				val addWikiIntent = Intent(this, AddWiki::class.java)
				startActivity(addWikiIntent)
				
				true
			}
			else -> super.onOptionsItemSelected(selectedItem)
		}
	}
	
	private fun populateWikiList()
	{
		Log.i(LogTag, "Populating wiki list.")
		
		val wikiList : MutableList<String> = prefs.GetWikiList()
		
		navigationDrawer.menu.removeGroup(R.id.nav_main_wikis)
		
		for(wikiName : String in wikiList) {
			val newItem : MenuItem = navigationDrawer.menu.add(
				R.id.nav_main_wikis,
				Menu.NONE, Menu.FIRST,
				wikiName
			)
			newItem.icon = ContextCompat.getDrawable(this, R.drawable.icon_wiki)
		}
	}
	
	private fun changeWiki(wikiName : String? = null) {
		lateinit var newWikiName : String
		if(wikiName != null) {
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
		
		val wikiData = prefs.GetCredentials(newWikiName)
		currentWiki = Wiki(this, newWikiName, wikiData)
		
		val wikiStatus = currentWiki!!.TestConnection()
		val pageList: List<String> = if(wikiStatus == ConnectionStatus.Ok)
			currentWiki!!.GetPageList(false)
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
			pageListFragment.PopulatePageList(pageList)
		})
	}
	
	override fun onPageSelection(item: WikiPageInfo) {
		TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
	}
}
