package com.sbrl.peppermint.display

import android.content.Intent
import android.webkit.JavascriptInterface
import com.sbrl.peppermint.activities.ViewPage

class ViewPageWebInterface(private val parent : ViewPage) {
	
	@Suppress("unused")
	@JavascriptInterface
	public fun ChangePage(newPageName : String) {
		val intent = Intent(parent, ViewPage::class.java)
		intent.putExtra(parent.INTENT_PARAM_WIKI_NAME, parent.GetCurrentWikiName())
		
		val parsedPageName = if (!newPageName.contains('#')) newPageName
			else newPageName.slice(0..newPageName.indexOf('#'))
		
		if(parsedPageName.isEmpty())
			return
		
		intent.putExtra(parent.INTENT_PARAM_PAGE_NAME, parsedPageName)
		
		if(newPageName.contains('#'))
			intent.putExtra(
				parent.INTENT_PARAM_PAGE_SECTION,
				newPageName.slice((newPageName.indexOf('#') + 1)..newPageName.length)
			)
		
		parent.startActivity(intent)
	}
	
	@Suppress("unused")
	@JavascriptInterface
	public fun NotifyRedlink(pageName : String) {
		parent.NotifyRedlink(pageName)
	}
}