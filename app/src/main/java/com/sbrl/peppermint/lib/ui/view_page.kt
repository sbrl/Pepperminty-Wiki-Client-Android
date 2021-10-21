package com.sbrl.peppermint.lib.ui

import android.content.Context
import android.content.Intent
import com.sbrl.peppermint.ui.EXTRA_PAGE_NAME
import com.sbrl.peppermint.ui.EXTRA_WIKI_ID
import com.sbrl.peppermint.ui.PageActivity
import com.sbrl.peppermint.ui.WikiViewModel

/**
 * Opens an activity to view the current page.
 * This has to be here because ViewModels are per-activity or per-fragment :-/
 * @param pagename: The name of the page to view.
 */
fun view_page(context: Context?, wikiViewModel: WikiViewModel, pagename: String) : Boolean {
	val currentWiki = wikiViewModel.currentWiki.value ?: return false
	val ctx = context ?: return false
	
	val intent = Intent(ctx, PageActivity::class.java).apply {
		putExtra(EXTRA_WIKI_ID, currentWiki.id)
		putExtra(EXTRA_PAGE_NAME, pagename)
	}
	
	ctx.startActivity(intent)
	return true
}