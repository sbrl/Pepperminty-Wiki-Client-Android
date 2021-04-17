package com.sbrl.peppermint.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sbrl.peppermint.lib.Wiki
import com.sbrl.peppermint.lib.WikiManager
import com.sbrl.peppermint.lib.io.DataManager

class WikiViewModel() : ViewModel() {
	val dataManager: DataManager = DataManager()
	
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
		return true
	}
	
	private val _wikiManager = MutableLiveData<WikiManager>().apply {
		value = WikiManager(dataManager)
		
	}
	
	val wikiManager: LiveData<WikiManager> = _wikiManager
	
	
	private val _currentWiki = MutableLiveData<Wiki>().apply {
		value = wikiManager.value?.currentWiki
		wikiManager.value?.wikiChanged?.on { _sender, args ->
			value = args.newCurrentWiki
		}
	}
	
	val currentWiki: LiveData<Wiki> = _currentWiki
	
}