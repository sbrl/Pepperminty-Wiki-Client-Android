package com.sbrl.peppermint.lib.io

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
	
	public fun hasCachedData(cache_id: String): Boolean {
		val cacheFile = getCacheFileHandle(cache_id)
		return cacheFile.exists()
	}
	
	public fun cacheString(cache_id: String, content: String) {
		val cacheFile: File = getCacheFileHandle(cache_id)
		setString(cacheFile, content)
	}
	
	public fun getCachedString(cache_id: String): String? {
		val cacheFile = getCacheFileHandle(cache_id)
		if(!cacheFile.exists())
			return null
		
		return getStringFromFile(cacheFile)
	}
	
	public fun cacheImage(cache_id: String, image: Bitmap) {
		val cacheFileHandle = getCacheFileHandle(cache_id)
		val cacheWriter = FileOutputStream(cacheFileHandle)
		image.compress(Bitmap.CompressFormat.WEBP, 95, cacheWriter)
	}
	
	public fun getCachedImage(cache_id: String): Bitmap? {
		val cacheFileHandle = getCacheFileHandle(cache_id)
		if (!cacheFileHandle.exists())
			return null
		
		return BitmapFactory.decodeStream(
			FileInputStream(cacheFileHandle)
		)
	}
	
	public fun storeString(storage_id: String, content: String) {
		val storageFile: File = getFileHandle(storage_id)
		setString(storageFile, content)
	}
	
	public fun getStoredString(storage_id: String): String? {
		val storageFile = getFileHandle(storage_id)
		if(!storageFile.exists())
			return null
		return getStringFromFile(storageFile)
	}
	
	public fun calculateCacheSize() : Long {
		return directorySize(context.cacheDir)
	}
	public fun clearCache() {
		clearDirectory(context.cacheDir)
	}
	
	// --------------------------------------------------------------------------------------------
	
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