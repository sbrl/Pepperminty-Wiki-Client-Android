package com.sbrl.peppermint.display

/**
 * From https://stackoverflow.com/a/24805871/1460422
 */
fun human_filesize(v: Long): String {
	if (v < 1024) return v.toString() + " B"
	val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
	return String.format("%.2f %siB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
}