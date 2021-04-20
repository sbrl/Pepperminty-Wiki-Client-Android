package com.sbrl.peppermint.ui.pageview

import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.sbrl.peppermint.R

class ViewPageJSInterface(private val parent : WebViewManager) {
	
	@Suppress("unused")
	@JavascriptInterface
	fun ChangePage(newPageName : String) {
		val hashLoc = newPageName.indexOf('#')
		
		val parsedPageName = if (hashLoc == -1) newPageName
			else newPageName.slice(0..newPageName.indexOf('#'))
		
		val parsedPageSection = if (hashLoc == -1) ""
			else newPageName.slice((newPageName.indexOf('#') + 1)..newPageName.length)
		
		if(parsedPageName.isEmpty())
			return
		
		parent.onLinkClicked.emit(parent, WebViewManager.OnLinkClickedEventArgs(
			parsedPageName,
			parsedPageSection
		))
	}
	
	@Suppress("unused")
	@JavascriptInterface
	fun NotifyRedlink(pagename : String) {
		Toast.makeText(parent.context,
			parent.context.getString(R.string.error_redlink, pagename),
			Toast.LENGTH_SHORT
		).show()
	}
}