package com.sbrl.peppermint.lib

import com.sbrl.peppermint.lib.helpers.HashMapBuilder


class Wiki(inName: String, inEndpoint: String, inCredentials: WikiCredentials?) {
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
		val response = api.makeGetRequest("list") ?: return null

		return response.body.lines()
	}
}