package com.sbrl.peppermint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sbrl.peppermint.lib.wiki_api.Wiki
import java.util.*

class PageViewModel() : ViewModel() {
	
	private val _stack: Stack<String> = Stack()
	
	private val _currentPageName = MutableLiveData<String>().apply {
		value = ""
	}
	
	/**
	 * The current page name.
	 */
	val currentPageName: LiveData<String> = _currentPageName
	
	/**
	 * Pushes a page onto the view stack.
	 */
	fun pushPage(pagename: String) {
		_stack.push(pagename)
		_currentPageName.value = pagename
	}
	
	/**
	 * Pops a page from the view stack.
	 * @return Whether a page was actually popped from the stack or not. A return value of false indicates that the stack was empty.
	 */
	fun popPage() : Boolean {
		if(_stack.size == 0) return false
		
		_currentPageName.value = _stack.pop()
		return true
	}
}