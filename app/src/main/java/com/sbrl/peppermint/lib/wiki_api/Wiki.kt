package com.sbrl.peppermint.lib.wiki_api

import android.util.Log
import org.json.JSONObject


class Wiki(inName: String, inEndpoint: String, inCredentials: WikiCredentials?) {
	constructor(inName: String, inEndpoint: String) : this(inName, inEndpoint, null)
	
	var api: WikiAPIBroker = WikiAPIBroker(inEndpoint, inCredentials)
	
	var name: String = inName
		get() = field
		set(value) { field = value }
	
	fun connectionOk(): ConnectionStatus {
		if(api.connectionStatus != ConnectionStatus.Untested)
			api.connectionStatus = api.testConnection()
		
		return api.connectionStatus
	}

	fun pages(): List<String>? {
		val response = api.makeGetRequest("list", mapOf<String, String>(
			"format" to "text"
		)) ?: return null
		Log.i("Wiki", "Page list: '${response.body.substring(0, 100)}' [truncated to 100 chars]")
		return response.body.lines()
	}
	
	// --------------------------------------------------------------------------------------------
	
	fun save() : JSONObject {
		val result = JSONObject()
		result.put("name", name)
		result.put("endpoint", api.endpoint)
		if(api.credentials !== null) {
			result.put("username", api.credentials!!.username)
			result.put("username", api.credentials!!.password)
		}
		return result
	}
	
	companion object {
		fun load(obj: JSONObject) : Wiki {
			// If any credentials are present, extract them
			val credentials = if(obj.has("username") && obj.has("password"))
				WikiCredentials(obj.getString("username"), obj.getString("password"))
			else null
			
			return Wiki(
				obj.getString("name"),
				obj.getString("endpoint"),
				credentials
			)
		}
	}
}