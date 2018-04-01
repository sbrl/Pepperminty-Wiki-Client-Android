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
		intent.putExtra(parent.INTENT_PARAM_PAGE_NAME, newPageName)
		
		parent.startActivity(intent)
	}
	
	@Suppress("unused")
	@JavascriptInterface
	public fun NotifyRedlink(pageName : String) {
		parent.NotifyRedlink(pageName)
	}
}