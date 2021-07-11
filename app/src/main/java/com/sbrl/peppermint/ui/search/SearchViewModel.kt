package com.sbrl.peppermint.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the search page. Coming soon!"
    }
    val text: LiveData<String> = _text
}