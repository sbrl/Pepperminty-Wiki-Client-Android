package com.sbrl.peppermint.bricks

import android.content.Context
import com.sbrl.peppermint.R
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

public class PageHTMLProcessor {
	private val context: Context
	
	private val htmlTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_template).readTextAndClose()
	private val cssTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_theme).readTextAndClose()
	private val jsTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display).readTextAndClose()
	
	/**
	 * Allows structural HTML + images, but not javascript
	 */
	private val htmlWhitelist = Whitelist.relaxed()
		.preserveRelativeLinks(true)
	
	constructor(context: Context) {
		this.context = context
	}
	
	public fun transform(pageHtml : String) : String {
		return sanitizeHTML(htmlTemplate)
			.replace("{footer}", footerInjectionCode())
			.replace("{content}", pageHtml)
	}
	
	private fun sanitizeHTML(html : String) : String {
		return Jsoup.clean(html, htmlWhitelist)
	}
	
	private fun footerInjectionCode() : String {
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