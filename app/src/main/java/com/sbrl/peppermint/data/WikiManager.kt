package com.sbrl.peppermint.data

import android.content.Context
import com.sbrl.peppermint.R
import com.sbrl.peppermint.activities.TemplateNavigation
import com.sbrl.peppermint.bricks.notify_send

class WikiManager(val activity : TemplateNavigation, val listener : WikiManagerEventListener) {
	var currentWiki : Wiki? = null
	
	
	init {
		setWiki("")
	}
	
	/**
	 * Sets the currently active wiki in the wiki manager.
	 * @return {Boolean}	Whether we were successful in setting the  currently active wiki (it may fail if the wiki doesn't exist in the preferences manager).
	 */
	fun setWiki(in_wiki_name: String) : Boolean {
		var wiki_name: String = resolveWikiName(in_wiki_name) ?: return false
		
		// We don't change wiki if there aren't any wikis registered and we haven't been
		// asked to switch to a specific wiki
		
		
		if(!activity.prefs.HasCredentials(wiki_name)) {
			activity.runOnUiThread {
				notify_send(activity, activity.getString(R.string.nav_unknown_wiki).replace("{0}", wiki_name))
			}
			return false
		}
		
		// We're clear to go ahead and switch to it - we think :P
		
		
		listener.onWikiChangePre(wiki_name)
		
		val wikiData = activity.prefs.GetCredentials(wiki_name)
		currentWiki = Wiki(activity, wiki_name, wikiData)
		
		listener.onWikiChangePost(wiki_name)
		
		return true
	}
	
	private fun AreWikisRegistered() : Boolean {
		return activity.prefs.GetWikiList().size > 0;
	}
	
	private fun resolveWikiName(wikiName: String) : String? {
		if(wikiName.isNotEmpty())
			return wikiName
		
		if (activity.intent.hasExtra(activity.INTENT_WIKI_NAME))
			return activity.intent.getStringExtra(activity.INTENT_WIKI_NAME)
		
		if(this.AreWikisRegistered())
			return activity.prefs.GetWikiList()[0]
		
		return null
	}
	
	interface WikiManagerEventListener {
		fun onWikiChangePre(wiki_name: String)
		fun onWikiChangePost(wiki_name: String)
	}
}