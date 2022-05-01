package com.sbrl.peppermint.lib.wiki_api

import java.time.LocalDateTime

enum class WikiRecentChangeType {
	Edit,
	Move,
	Upload,
	Delete,
	Comment;
	
	companion object {
		fun parse(str: String?): WikiRecentChangeType {
			if(str == null) return Edit
			return when(str) {
				"edit" -> Edit
				"upload" -> Upload
				"deletion" -> Delete
				"comment" -> Comment
				"move" -> Move
				else -> Edit
			}
		}
	}
}

class WikiRecentChange(
	val datetime: LocalDateTime,
	val type: WikiRecentChangeType,
	val pageName: String,
	val user: String,
	val payload: Any) {
	
	fun payloadEdit(): RecentChangePayloadEdit? {
		if (payload is RecentChangePayloadEdit) {
			return payload
		}
		return null
	}
	fun payloadMove(): RecentChangePayloadMove? {
		if (payload is RecentChangePayloadMove) {
			return payload
		}
		return null
	}
	fun payloadUpload(): RecentChangePayloadUpload? {
		if (payload is RecentChangePayloadUpload) {
			return payload
		}
		return null
	}
	fun payloadDelete(): RecentChangePayloadDelete? {
		if (payload is RecentChangePayloadDelete) {
			return payload
		}
		return null
	}
	fun payloadComment(): RecentChangePayloadComment? {
		if (payload is RecentChangePayloadComment) {
			return payload
		}
		return null
	}
}


class RecentChangePayloadEdit(
	val newSize: Int,
	val sizeDiff: Int,
	val isNewPage: Boolean
)

class RecentChangePayloadMove(
	val oldPageName: String
)

class RecentChangePayloadUpload(
	val fileSize: Int
)

class RecentChangePayloadDelete

class RecentChangePayloadComment(
	val commentId: String,
	val depth: Int
)