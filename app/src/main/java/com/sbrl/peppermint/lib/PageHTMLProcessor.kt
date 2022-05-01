package com.sbrl.peppermint.lib

import android.content.Context
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.io.readTextAndClose
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class PageHTMLProcessor(private val context: Context) {
	
	private val htmlTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_template).readTextAndClose()
	private val cssTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display_theme).readTextAndClose()
	private val jsTemplate : String get()
		= context.resources.openRawResource(R.raw.page_display).readTextAndClose()
	
	/**
	 * Allows structural HTML + images, but not javascript
	 */
	private val htmlSafelist = Safelist.relaxed()
		.preserveRelativeLinks(true)
	
	fun transform(pageHtml : String) : String {
		return sanitizeHTML(htmlTemplate)
			.replace("{footer}", footerInjectionCode())
			.replace("{content}", pageHtml)
	}
	
	private fun sanitizeHTML(html : String) : String {
		return Jsoup.clean(html, htmlSafelist)
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