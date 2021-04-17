package com.sbrl.peppermint.ui.pagelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageListViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the list of pages. Coming soon!"
    }
    val text: LiveData<String> = _text
}