package com.sbrl.peppermint.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sbrl.peppermint.lib.WikiManager
import com.sbrl.peppermint.lib.io.DataManager

class WikiViewModel(context: Context) : ViewModel() {
	private val dataManager: DataManager = DataManager(context)
	
	private val _wikiManager = MutableLiveData<WikiManager>().apply {
		value = WikiManager(dataManager)
	}
	
	val wikiManager: LiveData<WikiManager> = _wikiManager
}