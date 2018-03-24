package com.sbrl.peppermint.bricks

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL


class TextDownloader {
    val LogTag: String = "TextDownloader"

    fun Download(targetUrl: String): String {
        Log.i(LogTag, "Downloading $targetUrl")

        var result = ""

        val parsedUrl = URL(targetUrl)
        val remoteReader = BufferedReader(InputStreamReader(parsedUrl.openStream()))

        var nextLine: String? = remoteReader.readLine()
        do {
            result += nextLine
            nextLine = remoteReader.readLine()
        } while(nextLine != null)

        return result
    }
}