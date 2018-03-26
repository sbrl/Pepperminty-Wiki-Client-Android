package com.sbrl.peppermint.data

import android.provider.DocumentsContract
import org.json.JSONObject

class WikiCredentials
{
	private val JSON_PARAM_ROOT_URL = "root-url"
	private val JSON_PARAM_USERNAME = "username"
	private val JSON_PARAM_PASSWORD = "password"
	
	public val RootUrl : String
	public val Username : String?
	public val Password : String?
	
	constructor(inRootUrl: String) : this(inRootUrl, null, null)
	
	constructor(inRootUrl : String, inUsername : String?, inPassword: String?) {
		RootUrl = inRootUrl
		Username = inUsername
		Password = inPassword
	}
	constructor(source : JSONObject) {
		RootUrl = source.getString(JSON_PARAM_ROOT_URL)
		Username = source.getString(JSON_PARAM_USERNAME)
		Password = source.getString(JSON_PARAM_PASSWORD)
	}
	
	public fun toJSON() : JSONObject {
		val result = JSONObject()
		
		result.put(JSON_PARAM_ROOT_URL, RootUrl)
		result.put(JSON_PARAM_USERNAME, Username)
		result.put(JSON_PARAM_PASSWORD, Password)
		
		return result;
	}
}