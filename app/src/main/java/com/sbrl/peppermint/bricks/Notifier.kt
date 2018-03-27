package com.sbrl.peppermint.bricks

import android.content.Context
import android.widget.Toast

fun notify_send(context: Context, message: String) {
	Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}