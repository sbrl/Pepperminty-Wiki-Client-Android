package com.sbrl.peppermint.lib.wiki_api

import okhttp3.Headers
import okhttp3.Response

class WikiApiResponse(response: Response) {
	val statusCode = response.code
		get() = field
	
	val body = response.body!!.string()
		get() = field
	
	val headers: Headers = response.headers
	
	fun isLoginRequired() : Boolean {
		return hasHeader("x-login-required")
	}
	
	fun hasHeader(headerName: String) : Boolean {
		return headers[headerName] !== null
	}
}