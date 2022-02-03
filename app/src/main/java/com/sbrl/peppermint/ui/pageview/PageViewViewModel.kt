package com.sbrl.peppermint.ui.pageview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageViewViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the page view page. Coming soon!"
    }
    val text: LiveData<String> = _text
}