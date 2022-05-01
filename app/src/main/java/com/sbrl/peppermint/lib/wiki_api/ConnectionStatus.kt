package com.sbrl.peppermint.lib.wiki_api

enum class ConnectionStatus {
	// The connection is untested.
	Untested,
	// The connection is ok, everything is good
	Ok,
	// The connection is ok, but credentials are required to access the wiki
	CredentialsRequired,
	// The wiki exists and credentials were provided, but when we tried to login it failed
	CredentialsIncorrect,
	// The connection failed for an unknown/undefined reason
	ConnectionFailed
}