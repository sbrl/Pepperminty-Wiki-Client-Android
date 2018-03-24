package com.sbrl.peppermint.data

import android.provider.DocumentsContract
import org.json.JSONObject

class WikiCredentials
{
	public val RootUrl : String
	public val Username : String
	public val Password : String
	
	constructor(inRootUrl : String, inUsername : String, inPassword: String) {
		RootUrl = inRootUrl
		Username = inUsername
		Password = inPassword
	}
	constructor(source : JSONObject) {
		RootUrl = source.getString("root-url")
		Username = source.getString("username")
		Password = source.getString("password")
	}
	
	public fun toJSON() : JSONObject {
		val result = JSONObject()
		
		result.put("username", Username)
		result.put("password", Password)
		
		return result;
	}
}