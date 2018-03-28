package com.sbrl.peppermint.bricks

import java.io.InputStream
import java.nio.charset.Charset

/**
 * From https://stackoverflow.com/a/39500046/1460422
 */
fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
	return this.bufferedReader(charset).use { it.readText() }
}