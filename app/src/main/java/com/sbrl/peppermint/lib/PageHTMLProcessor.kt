package com.sbrl.peppermint.lib

import android.content.Context
import android.util.Log
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.io.SettingsManager
import com.sbrl.peppermint.lib.io.readTextAndClose
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class PageHTMLProcessor(private val context: Context) {
	private val settings = SettingsManager(context)
	
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
		.addAttributes("a", "class")
		.addAttributes("div", "class")
		.addAttributes("a", "href")
		.addAttributes("img", "src")
		.addAttributes("h1", "id")
		.addAttributes("h2", "id")
		.addAttributes("h3", "id")
		.addAttributes("h4", "id")
		.addAttributes("h5", "id")
		.addAttributes("h6", "id")
		.addProtocols("a", "href", "https", "http", "#")
		.addProtocols("img", "src", "https", "http", "#")
	
	fun transform(endpoint : String, pageHtml : String) : String {
		Log.i("PageHTMLProcessor", "MODE "+if (settings.isDark) "dark" else "light")
		return htmlTemplate
			.replace("{footer}", footerInjectionCode())
			.replace("{content}", sanitizeHTML(endpoint, pageHtml))
			.replace("{mode}", if (settings.isDark) "dark" else "light")
	}
	
	private fun sanitizeHTML(endpoint : String, html : String) : String {
		return Jsoup.clean(html, endpoint, htmlSafelist)
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