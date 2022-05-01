package com.sbrl.peppermint.ui.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.ui.show_toast
import com.sbrl.peppermint.ui.PageViewModel
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class EditFragment : Fragment() {
	
	private lateinit var pageViewModel: PageViewModel
	private lateinit var wikiViewModel: WikiViewModel
	
	private lateinit var editorMain : EditText
	private lateinit var buttonSave : Button
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// 1: Fetch the ViewModels
		// Each context gets it's own ViewModel instance
		pageViewModel = ViewModelProvider(requireActivity())[PageViewModel::class.java]
		wikiViewModel = ViewModelProvider(requireActivity())[WikiViewModel::class.java]
		
		
		// 2: Inflate the layout
		val root = inflater.inflate(R.layout.fragment_edit, container, false)
		
		// 3: Find ids
		editorMain = root.findViewById(R.id.editor_editor)
		buttonSave = root.findViewById(R.id.editor_button_save)
		
		
		// 4: Listeners
		buttonSave.setOnClickListener {	buttonClickSave(it) }
		
		
		// 5: Initial UI population
		populateEditor()
		
		return root
	}
	
	private fun populateEditor() {
		if(wikiViewModel.settings.offline) {
			show_toast(context, getString(R.string.editor_toast_error_offline))
			return
		}
		val pagename: String? = pageViewModel.currentPageName.value
		if(pagename == null) {
			show_toast(context, getString(R.string.editor_toast_error_nopagename))
			return
		}
		
		thread {
			val source = wikiViewModel.currentWiki.value!!.pageSource(pagename)
			
			activity?.runOnUiThread {
				if(source == null) {
					show_toast(context, getString(R.string.error_failed_load_page_source, pagename))
					editorMain.isEnabled = false
					return@runOnUiThread
				}
				editorMain.isEnabled = true
				
				(requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.editor_title, pageViewModel.currentPageName.value ?: "")
				
				editorMain.setText(source.value)
			}
			
		}
	}
	
	private fun buttonClickSave(view: View) {
		show_toast(context, "Coming soon! This hasn't been implemented yet.")
	}
}