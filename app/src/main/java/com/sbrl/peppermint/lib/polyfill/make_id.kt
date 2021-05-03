package com.sbrl.peppermint.lib.polyfill

/**
 * Makes a new random case-insensitive alphanumeric string.
 * Ref https://stackoverflow.com/a/54400933/1460422
 * @param length: The length of string to generate.
 */
fun make_id(length: Int) : String {
	val allowedChars = ('a'..'z') + ('0'..'9')
	return (1..length)
		.map { allowedChars.random() }
		.joinToString("")
}