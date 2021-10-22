package com.sbrl.peppermint.lib.ui

import android.content.Context
import android.view.Gravity
import android.widget.Toast

fun show_toast(context: Context?, msg: String) {
	val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
	// This doesn't have any effect :-(
	// We should implement a SnackBar instead: https://developer.android.com/training/snackbar/showing
	//toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
	toast.show()
}