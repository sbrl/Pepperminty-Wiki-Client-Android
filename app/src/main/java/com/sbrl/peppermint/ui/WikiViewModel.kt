package com.sbrl.peppermint.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.lib.wiki_api.WikiManager
import com.sbrl.peppermint.lib.io.DataManager
import com.sbrl.peppermint.lib.io.SettingsManager

class WikiViewModel() : ViewModel() {
	var isSetup: Boolean = false
	val dataManager: DataManager = DataManager()
	lateinit var settings: SettingsManager
	
	init {
		Log.i("WikiViewModel", "init")
	}
	
	/**
	 * Initialises this WikiViewModel instance.
	 * The context is only needed to determine the directories in which we should put our stuff.
	 * @param context: A Context instance, because apparently you can't figure out where to put stuff on disk without it... If null is passed, then no setup is performed.
	 */
	fun init(context: Context?) : Boolean {
		if(isSetup) {
			Log.d("WikiViewModel", "We have already completed init, refusing to init again")
			return true
		}
		if(context == null) return false
		settings = SettingsManager(context)
		
		dataManager.init(
			context.cacheDir,
			context.filesDir
		)
		// Now that the DataManager has been initialised, we can create the WikiManager
		_wikiManager.value = WikiManager(settings, dataManager)
		// Update the current wiki
		_currentWiki.value = wikiManager.value?.currentWiki
		
		wikiManager.value?.wikiChanged?.on { _sender, args ->
			Log.i("WikiViewModel", "Wiki changed, new wiki ${args.newCurrentWiki.name} (#${args.newCurrentWiki.id}")
			
			_currentWiki.value = args.newCurrentWiki
		}
		
		Log.i("WikiViewModel", "Initialisation complete")
		
		isSetup = true
		
		return true
	}
	
	private val _wikiManager = MutableLiveData<WikiManager>()
	
	val wikiManager: LiveData<WikiManager> = _wikiManager
	
	
	private val _currentWiki = MutableLiveData<Wiki>()
	
	val currentWiki: LiveData<Wiki> = _currentWiki
	
}