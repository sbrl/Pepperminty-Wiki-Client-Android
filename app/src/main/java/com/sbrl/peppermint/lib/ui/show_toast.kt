package com.sbrl.peppermint.lib.ui

import android.content.Context
import android.view.Gravity
import android.widget.Toast

fun show_toast(context: Context?, msg: String): Unit {
	val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
	toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
	toast.show()
}