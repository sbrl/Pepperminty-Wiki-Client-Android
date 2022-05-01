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
import java.util.*

class DataManager() {
	private var cacheDir: File? = null
	private var filesDir: File? = null
	
	/**
	 * The compression format to use for storing images.
	 */
	private val compressFormat: Bitmap.CompressFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R)
		Bitmap.CompressFormat.WEBP_LOSSLESS
	else
		Bitmap.CompressFormat.WEBP
	
	public fun init(inCacheDir: File, inFilesDir: File) {
		cacheDir = inCacheDir
		filesDir = inFilesDir
	}
	
	public fun hasCachedData(namespace: String, cache_id: String): Boolean {
		val cacheFile = getCacheFileHandle(namespace, cache_id)
		return cacheFile.exists()
	}
	
	public fun cacheString(namespace: String, cache_id: String, content: String) {
		val cacheFile: File = getCacheFileHandle(namespace, cache_id)
		setString(cacheFile, content)
	}
	
	/**
	 * Fetches the mtime the given cache item was last modified.
	 * @param cache_id: The cache id of the item to retrieve the mtime for.
	 */
	public fun getCachedMtime(namespace: String, cache_id: String) : Date? {
		if(!hasCachedData(namespace, cache_id)) return null
		return Date(getCacheFileHandle(namespace, cache_id).lastModified())
	}
	
	/**
	 * Fetches the content associated with a given cache id as a string.
	 * @param cache_id: The cache id of the item to retrieve the contents as a string for.
	 */
	public fun getCachedString(namespace: String, cache_id: String): String? {
		val cacheFile = getCacheFileHandle(namespace, cache_id)
		if(!cacheFile.exists())
			return null
		
		return getStringFromFile(cacheFile)
	}
	
	/**
	 * Saves an image in to the cache with the specified cache id.
	 * @param cache_id: The cache id to save the image with.
	 * @param image: The image to save.
	 */
	public fun cacheImage(namespace: String, cache_id: String, image: Bitmap) {
		val cacheFileHandle = getCacheFileHandle(namespace, cache_id)
		val cacheWriter = FileOutputStream(cacheFileHandle)
		
		image.compress(compressFormat, 95, cacheWriter)
	}
	
	/**
	 * Fetches the content associated with a given cache id as an image.
	 * @param cache_id: The cache id of the item to retrieve the contents as an image.
	 */
	public fun getCachedImage(namespace: String, cache_id: String): Bitmap? {
		val cacheFileHandle = getCacheFileHandle(namespace, cache_id)
		if (!cacheFileHandle.exists())
			return null
		
		return BitmapFactory.decodeStream(
			FileInputStream(cacheFileHandle)
		)
	}
	
	public fun hasStoredData(namespace: String, storage_id: String): Boolean {
		val storageFile = getFileHandle(namespace, storage_id)
		return storageFile.exists()
	}
	
	public fun storeString(namespace: String, storage_id: String, content: String) {
		val storageFile: File = getFileHandle(namespace, storage_id)
		setString(storageFile, content)
	}
	
	public fun getStoredString(namespace: String, storage_id: String): String? {
		val storageFile = getFileHandle(namespace, storage_id)
		if(!storageFile.exists())
			return null
		return getStringFromFile(storageFile)
	}
	
	public fun calculateCacheSize() : Long {
		return directorySize(cacheDir!!)
	}
	public fun clearCache() {
		clearDirectory(cacheDir!!)
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
	
	private fun getCacheFileHandle(namespace: String, cache_id: String): File {
		// 1: Create the namespace directory if it exists
		val dir = File(cacheDir!!, slugify(namespace))
		if(!dir.isDirectory)
			dir.mkdir()
		// 2: Calculate the filename
		val filename = slugify(cache_id) + ".cache"
		// 3: Return the file instance
		return File(dir, filename)
	}
	
	private fun getFileHandle(namespace: String, storage_id: String): File {
		// 1: Create the namespace directory if it exists
		val dir = File(filesDir!!, slugify(namespace))
		if(!dir.isDirectory)
			dir.mkdir()
		// 2: Calculate the filename
		val filename = "${slugify(namespace)}/${slugify(storage_id)}"
		// 3: Return the file instance
		// NOTE: We don't use getDataDir() here - even conditionally - because getFilesDir() gets the location the use prefers us to store stuff.
		return File(filesDir!!, filename)
	}
	
	/**
	 * Makes a string safe to be contained within a filename.
	 * @param str: The string to convert.
	 * @return The safe slugified value.
	 */
	private fun slugify(str: String): String {
		return str.replace("[^a-z0-9\\-_$]+".toRegex(), "-")
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