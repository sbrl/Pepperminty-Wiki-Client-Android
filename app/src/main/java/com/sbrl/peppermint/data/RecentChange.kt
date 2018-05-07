package com.sbrl.peppermint.data

import java.util.*

public open class RecentChange(
	Timestamp: Date,
	Type: String,
	PageName: String,
	User: String
) {

}

public class RecentChangeEdit(
	Timestamp: Date,
	PageName: String,
	User: String,
	NewSize : Int,
	SizeDiff : Int,
	NewPage : Boolean = false
) : RecentChange(Timestamp, "edit", PageName, User) {

}

public class RecentChangeMove(
	Timestamp: Date,
	PageName: String,
	User: String,
	OldPageName : String
) : RecentChange(Timestamp, "move", PageName, User) {

}

public class RecentChangeUpload(
	Timestamp: Date,
	PageName: String,
	User: String,
	Filesize: Int
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
	CommentId : String,
	ReplyDepth : Int
) : RecentChange(Timestamp, "comment", PageName, User) {

}
