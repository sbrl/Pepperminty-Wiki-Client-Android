package com.sbrl.peppermint.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.lib.wiki_api.WikiManager
import com.sbrl.peppermint.lib.io.DataManager

class WikiViewModel() : ViewModel() {
	val dataManager: DataManager = DataManager()
	
	init {
		Log.i("WikiViewModel", "init")
	}
	
	/**
	 * Initialises this WikiViewModel instance.
	 * The context is only needed to determine the directories in which we should put our stuff.
	 * @param context: A Context instance, because apparently you can't figure out where to put stuff on disk without it... If null is passed, then no setup is performed.
	 */
	fun init(context: Context?) : Boolean {
		if(context == null) return false
		
		dataManager.init(
			context.cacheDir,
			context.filesDir
		)
		// Now that the DataManager has been initialised, we can create the WikiManager
		_wikiManager.value = WikiManager(dataManager)
		// Update the current wiki
		_currentWiki.value = wikiManager.value?.currentWiki
		
		Log.i("WikiViewModel", "Initialisation complete")
		
		return true
	}
	
	private val _wikiManager = MutableLiveData<WikiManager>()
	
	val wikiManager: LiveData<WikiManager> = _wikiManager
	
	
	private val _currentWiki = MutableLiveData<Wiki>().apply {
		wikiManager.value?.wikiChanged?.on { _sender, args ->
			value = args.newCurrentWiki
		}
	}
	
	val currentWiki: LiveData<Wiki> = _currentWiki
	
}