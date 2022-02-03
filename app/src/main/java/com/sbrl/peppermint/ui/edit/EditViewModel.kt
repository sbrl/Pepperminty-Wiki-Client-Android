package com.sbrl.peppermint.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the edit page. Coming soon!"
    }
    val text: LiveData<String> = _text
}