package com.sbrl.peppermint.bricks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter

class DataManager(private val context: Context) {
	val LogTag = "DataManager"
	
	public fun HasCachedData(cache_id: String): Boolean {
		val cacheFile = getCacheFileHandle(cache_id)
		return cacheFile.exists()
	}
	
	public fun CacheString(cache_id: String, content: String) {
		val cacheFile: File = getCacheFileHandle(cache_id)
		setString(cacheFile, content)
		
		Log.i(LogTag, String.format(
				"Cache: Cached %d bytes to %s.",
				content.length,
				cache_id
		))
	}
	
	public fun GetCachedString(cache_id: String): String? {
		val cacheFile = getCacheFileHandle(cache_id)
		if(!cacheFile.exists())
			return null
		
		return getStringFromFile(cacheFile)
	}
	
	public fun CacheImage(cache_id: String, image: Bitmap) {
		val cacheFileHandle = getCacheFileHandle(cache_id)
		val cacheWriter = FileOutputStream(cacheFileHandle)
		image.compress(Bitmap.CompressFormat.WEBP, 95, cacheWriter)
	}
	
	public fun GetCachedImage(cache_id: String): Bitmap? {
		val cacheFileHandle = getCacheFileHandle(cache_id)
		if (!cacheFileHandle.exists())
			return null
		
		return BitmapFactory.decodeStream(
			FileInputStream(cacheFileHandle)
		)
	}
	
	public fun StoreString(storage_id: String, content: String) {
		val storageFile: File = getFileHandle(storage_id)
		setString(storageFile, content)
		
		Log.i(LogTag, String.format(
				"Internal Storage: Written %d bytes to %s.",
				content.length,
				storage_id
		))
	}
	
	public fun GetStoredString(storage_id: String): String? {
		val storageFile = getFileHandle(storage_id)
		if(!storageFile.exists())
			return null
		return getStringFromFile(storageFile)
	}
	
	public fun CalculateCacheSize() : Long {
		return directorySize(context.cacheDir)
	}
	public fun ClearCache() {
		clearDirectory(context.cacheDir)
	}
	
	@Throws(IOException::class)
	private fun getStringFromFile(handle: File): String {
		val storageReader = BufferedReader(
			InputStreamReader(
				FileInputStream(handle)
			)
		)
		
		val result = storageReader.readText()
		storageReader.close()
		
		return result
	}
	
	
	private fun setString(handle: File, data: String) {
		val writer = PrintWriter(handle)
		writer.print(data)
		writer.close()
	}
	
	private fun getCacheFileHandle(cache_id: String): File {
		val filename = slugify(cache_id) + ".cache"
		return File(context.cacheDir, filename)
	}
	
	private fun getFileHandle(storage_id: String): File {
		val filename = slugify(storage_id)
		// NOTE: We don't use getDataDir() here - even conditionally - because getFilesDir() gets the location the use prefers us to store stuff.
		return File(context.filesDir, filename)
	}
	
	private fun slugify(str: String): String {
		return str.replace("[^a-z0-9\\-]+".toRegex(), "-")
	}
	
	private fun directorySize(dir : File) : Long {
		var totalSize = 0L
		for(nextFile : File in dir.walk()) {
			Log.i(LogTag, "File: ${nextFile.absolutePath}")
			totalSize += nextFile.length()
		}
		return totalSize
	}
	/**
	 * Clears out a directory of all it's content.
	 * @param	dir		The directory to clear.
	 */
	private fun clearDirectory(dir : File) {
		dir.deleteRecursively()
	}
}
