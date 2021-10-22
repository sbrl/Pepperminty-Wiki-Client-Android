package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.*
import java.time.LocalDateTime
import kotlin.reflect.typeOf

private fun parse_recent_change(change: JSONObject) : WikiRecentChange {
	val type = WikiRecentChangeType.parse(
		if(change.has("type")) change.getString("type") else "unknown"
	)
	val datetime = parse_datetime(change.get("timestamp")) ?: LocalDateTime.of(
		1970, 0, 1,
		0, 1, 0)
	val pageName = change.getString("page")
	val user = change.getString("user")
	
	val payload: Any = when(type) {
		WikiRecentChangeType.Move -> RecentChangePayloadMove(
			change.getString("oldpage")
		)
		WikiRecentChangeType.Delete -> RecentChangePayloadDelete()
		WikiRecentChangeType.Upload -> RecentChangePayloadUpload(
			change.getInt("filesize")
		)
		WikiRecentChangeType.Comment -> RecentChangePayloadComment(
			change.getString("comment_id"),
			change.getInt("reply_depth")
		)
		else -> RecentChangePayloadEdit(
			change.getInt("newsize"),
			change.getInt("sizediff"),
			change.has("newpage") && change.getBoolean("newpage")
		)
	}
	
	return WikiRecentChange(datetime, type, pageName, user, payload)
}

private fun parse_datetime(datetime: Any) : LocalDateTime? {
	Log.i("parse_datetime", "Source: $datetime")
	if(datetime is Long || datetime is Int) {
		val number: Long = if(datetime is Long) datetime else (datetime as Int).toLong()
		Log.i("parse_datetime", "number: $number")
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(number), ZoneOffset.UTC)
	}
	
	if(datetime is String)
		return LocalDateTime.parse(datetime)
	
	Log.i("parse_datetime", "Don't know what it is, so we can't parse it")
	
	return null
}

/**
 * Parses a list of recent changes stored in a string of JSON.
 */
fun parse_recent_changes(jsonText: String): List<WikiRecentChange> {
	val result = mutableListOf<WikiRecentChange>()
	val changes = JSONArray(jsonText)
	
	for (i in 0 until changes.length()) {
		val next = changes.getJSONObject(i)
		result.add(parse_recent_change(next))
	}
	return result
}