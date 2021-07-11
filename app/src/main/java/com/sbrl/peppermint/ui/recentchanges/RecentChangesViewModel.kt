package com.sbrl.peppermint.ui.recentchanges

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecentChangesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the recent changes page. Coming soon!"
    }
    val text: LiveData<String> = _text
}