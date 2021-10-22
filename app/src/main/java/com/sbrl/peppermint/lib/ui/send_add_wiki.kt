package com.sbrl.peppermint.lib.ui

import android.content.Context
import android.content.Intent
import com.sbrl.peppermint.ui.EXTRA_SWAP_MAIN
import com.sbrl.peppermint.ui.addwiki.AddWikiActivity

fun send_add_wiki(context: Context?, clear_stack: Boolean = false): Boolean {
	val ctx: Context = context ?: return false
	
	val intent = Intent(ctx, AddWikiActivity::class.java)
	
	if(clear_stack) {
		intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
		intent.putExtra(EXTRA_SWAP_MAIN, true)
	}
	
	ctx.startActivity(intent)
	
	return true
}