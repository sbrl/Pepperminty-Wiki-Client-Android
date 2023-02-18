package com.sbrl.peppermint.lib.wiki_api

import com.sbrl.peppermint.R

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
	ConnectionFailed;
	
	companion object {
		fun toMessage(status: ConnectionStatus): Int {
			return when(status) {
				Ok -> R.string.connection_ok
				CredentialsRequired -> R.string.login_required
				CredentialsIncorrect ->
					R.string.login_failed
				ConnectionFailed ->
					R.string.connection_failed
				Untested ->
					R.string.connection_untested
			}
		}
	}
}