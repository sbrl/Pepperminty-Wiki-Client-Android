package com.sbrl.peppermint.lib.polyfill

import android.util.Log
import java.time.*
import java.time.temporal.Temporal
import java.util.*
import kotlin.math.floor


/**
 * From https://stackoverflow.com/a/24805871/1460422
 */
fun human_filesize(v: Long): String {
	if (v < 1024) return "$v B"
	val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
	return String.format("%.2f %siB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
}

fun human_time_since(date: LocalDateTime) : String {
	val secondsNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
	val secondsThen = date.toEpochSecond(ZoneOffset.UTC)
	val secondsDifferent = secondsNow - secondsThen
	return human_time(
		secondsDifferent
	)
}

fun human_time(seconds : Long) : String {
	val times = mapOf( // TODO: Extract these as string resources
		31536000f	to "year",
		2592000f	to "month",
		604800f		to "week",
		86400f		to "day",
		3600f		to "hour",
		60f			to "minute",
		1f			to "second"
	)
	for(entry in times) {
		if(seconds < entry.key) continue
		val unitCount = floor(seconds.toFloat() / entry.key).toInt()
		return "$unitCount ${entry.value}${if(unitCount != 1) "s" else ""}"
	}
	return "just now"
}