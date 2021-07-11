package com.sbrl.peppermint.lib.polyfill

import android.util.Patterns
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Checks whether the given string is a valid URL or not.
 * @param str   The string to check.
 * @return Whether the given string is a valid URL or not.
 */
fun is_valid_url(str: String): Boolean {
	val p: Pattern = Patterns.WEB_URL
	val m: Matcher = p.matcher(str.toLowerCase(Locale.getDefault()))
	return m.matches()
}