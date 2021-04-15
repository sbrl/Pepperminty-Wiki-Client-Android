package com.sbrl.peppermint.lib

import okhttp3.Headers
import okhttp3.Response

class WikiApiResponse(response: Response) {
	val statusCode = response.code
		get() = field
	
	val body = response.body!!.string()
		get() = field
	
	private val headers: Headers = response.headers
	
	fun isLoginRequired() : Boolean {
		return headers["x-login-required"] == null
	}
}