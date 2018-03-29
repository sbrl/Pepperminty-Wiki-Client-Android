package com.sbrl.peppermint.bricks

import android.content.Context
import com.sbrl.peppermint.R

public class PageHTMLProcessor {
	private val context: Context
	
	private val htmlTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_template).readTextAndClose()
	private val cssTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_theme).readTextAndClose()
	private val jsTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display).readTextAndClose()
	
	constructor(context: Context) {
		this.context = context
	}
	
	public fun transform(pageHtml : String) : String {
		// TODO: Strip <script> tags, javascript: urls, and on* attributes from raw pageHTML
		return htmlTemplate
			.replace("{footer}", footerInjectionCode())
			.replace("{content}", pageHtml)
	}
	
	protected fun footerInjectionCode() : String {
		val result = StringBuilder()
		result.append("<style>")
		result.append(cssTemplate)
		result.append("</style>")
		
		result.append("<script>")
		result.append(jsTemplate)
		result.append("</script>")
		
		return result.toString()
	}
}