package com.sbrl.peppermint.data

import java.util.*

public open class RecentChange(
	public val Timestamp: Date,
	public val Type: String,
	public val PageName: String,
	public val User: String
) {

}

public class RecentChangeEdit(
	Timestamp: Date,
	PageName: String,
	User: String,
	public val NewSize : Int,
	public val SizeDiff : Int,
	public val NewPage : Boolean = false
) : RecentChange(Timestamp, "edit", PageName, User) {

}

public class RecentChangeMove(
	Timestamp: Date,
	PageName: String,
	User: String,
	public val OldPageName : String
) : RecentChange(Timestamp, "move", PageName, User) {

}

public class RecentChangeUpload(
	Timestamp: Date,
	PageName: String,
	User: String,
	public val Filesize: Int
) : RecentChange(Timestamp, "upload", PageName, User) {

}

public class RecentChangeDeletion(
	Timestamp: Date,
	PageName: String,
	User: String
) : RecentChange(Timestamp, "deletion", PageName, User) {

}


public class RecentChangeComment(
	Timestamp: Date,
	PageName: String,
	User: String,
	public val CommentId : String,
	public val ReplyDepth : Int
) : RecentChange(Timestamp, "comment", PageName, User) {

}
