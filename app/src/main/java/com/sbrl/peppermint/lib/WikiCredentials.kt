package com.sbrl.peppermint.lib

class WikiCredentials(inUsername: String, inPassword: String) {
	var username: String = inUsername
		get() = field
		set(value) { field = value }
	
	var password: String = inPassword
		get() = field
		set(value) { field = value }
}