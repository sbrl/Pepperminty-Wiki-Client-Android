package com.sbrl.peppermint.display

import java.lang.Math.floor
import java.util.*
import kotlin.math.floor

/**
 * From https://stackoverflow.com/a/24805871/1460422
 */
fun human_filesize(v: Long): String {
	if (v < 1024) return v.toString() + " B"
	val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
	return String.format("%.2f %siB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
}

fun human_time_since(date : Date) : String {
	return human_time(floor((Date().time / 1000f) - (date.time / 1000f)).toLong())
}

fun human_time(seconds : Long) : String {
	val times = mapOf(
		31536000f	to "year",
		2592000f	to "month",
		604800f		to "week",
		86400f		to "day",
		3600f		to "hour",
		60f			to "minute",
		1f			to "second"
	)
	var result = ""
	for(timespan in times) {
		if(seconds < timespan.key) continue
		val unitCount = floor(seconds.toFloat() / timespan.key)
		return "$unitCount ${timespan.value}${if(unitCount > 0) "s" else ""}"
	}
	return "just now"
}