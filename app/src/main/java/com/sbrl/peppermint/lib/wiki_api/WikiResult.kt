package com.sbrl.peppermint.lib.wiki_api

import android.content.Context
import com.sbrl.peppermint.R

class WikiResult<T>(val source: Wiki.Source,
					val value: T,
					val error: WikiError = WikiError.None,
					val requiredVersion: WikiVersion? = null
) {
	
	
	val ok: Boolean
		get() = error == WikiError.None
	
	fun errorText(context: Context?) : String {
		if(context == null) return "No context object provided. This is a bug."
		
		return when(error) {
			WikiError.NetworkError -> context.getString(R.string.wikiresult_error_network)
			WikiError.NetworkErrorAndNoCache -> context.getString(R.string.wikiresult_error_networknocache)
			WikiError.OutdatedServer -> context.getString(R.string.wikiresult_error_outdatedserver, requiredVersion)
			WikiError.ConflictError -> context.getString(R.string.wikiresult_error_edit_conflict)
			WikiError.NoEditsAllowedError -> context.getString(R.string.wikiresult_error_noeditsallowed)
			WikiError.PageProtectedError -> context.getString(R.string.wikiresult_error_pageprotected)
			WikiError.UnknownClientError -> context.getString(R.string.wikiresult_error_unknownclient)
			WikiError.ServerError -> context.getString(R.string.wikiresult_error_server)
			WikiError.PermissionsError -> context.getString(R.string.wikiresult_error_permissions)
			else -> context.getString(R.string.wikiresult_error_unknown)
		}
	}
	
	companion object {
		/**
		 * Convenience function for generating WikiResult instances for errors.
		 * The wiki source will automatically be set to Source.Internet.
		 */
		fun<T> Error(error: WikiError, requiredVersion: WikiVersion? = null) : WikiResult<T?> {
			return WikiResult(
				Wiki.Source.Internet,
				value = null,
				error = error,
				requiredVersion = requiredVersion
			)
		}
		/**
		 * Convenience function for generating WikiResult instances for errors.
		 * The wiki source will automatically be set to Source.Cache.
		 */
		fun<T> CacheError(error: WikiError, requiredVersion: WikiVersion? = null) : WikiResult<T?> {
			return WikiResult(
				Wiki.Source.Cache,
				value = null,
				error = error,
				requiredVersion = requiredVersion
			)
		}
		
	}
}

enum class WikiError {
	None,
	OutdatedServer,
	NetworkError,
	NetworkErrorAndNoCache,
	ServerError,
	PermissionsError,
	PageProtectedError,
	NoEditsAllowedError,
	ConflictError,
	UnknownClientError,
	UnknownError
}