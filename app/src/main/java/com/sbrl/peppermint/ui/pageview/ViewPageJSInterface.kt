package com.sbrl.peppermint.ui.pageview

import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.sbrl.peppermint.R

class ViewPageJSInterface(private val parent : PageViewFragment) {
	
	@Suppress("unused")
	@JavascriptInterface
	fun ChangePage(newPageName : String) {
		val parsedPageName = if (!newPageName.contains('#')) newPageName
		else newPageName.slice(0..newPageName.indexOf('#'))
		
		val parsedPageSection = newPageName.slice((newPageName.indexOf('#') + 1)..newPageName.length)
		
		if(parsedPageName.isEmpty())
			return
		
		parent.pushPage(parsedPageName, parsedPageSection)
	}
	
	@Suppress("unused")
	@JavascriptInterface
	fun NotifyRedlink(pagename : String) {
		Toast.makeText(parent.context,
			parent.getString(R.string.error_redlink, pagename),
			Toast.LENGTH_SHORT
		).show()
	}
}