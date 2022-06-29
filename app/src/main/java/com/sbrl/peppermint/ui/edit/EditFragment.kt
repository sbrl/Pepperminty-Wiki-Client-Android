package com.sbrl.peppermint.ui.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
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
	private lateinit var editorTags : MultiAutoCompleteTextView
	private lateinit var buttonSave : Button
	
	private var editKey: String? = null
	
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
		editorTags = root.findViewById(R.id.editor_tags)
		buttonSave = root.findViewById(R.id.editor_button_save)
		
		
		// 4: Listeners
		buttonSave.setOnClickListener {	buttonClickSave(it) }
		
		
		// 5: Initial UI population
		editorTags.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
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
			val wiki = wikiViewModel.currentWiki.value!!
			val source = wiki.pageSource(pagename)
			val newEditKey = wiki.editKey(pagename)
			val tagList = wiki.tags()
			
			activity?.runOnUiThread {
				editorMain.isEnabled = false
				editorTags.isEnabled = false
				
				if(!source.ok) {
					show_toast(context, getString(R.string.error_failed_load_page_source, 
						pagename,
						source.errorText(context)
					))
					return@runOnUiThread
				}
				if(!tagList.ok) {
					show_toast(context, getString(
						R.string.error_failed_load_tag_list,
						tagList.errorText(context)
					))
					return@runOnUiThread
				}
				if(newEditKey == null) {
					show_toast(context, getString(R.string.error_failed_acquire_edit_key, pagename))
					return@runOnUiThread
				}
				
				val page = source.value!!
				
				editorMain.isEnabled = true
				editorTags.isEnabled = true
				
				(requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.editor_title, pageViewModel.currentPageName.value ?: "")
				
				val contextTemp = context
				if(contextTemp !== null)
					editorTags.setAdapter(ArrayAdapter(
						contextTemp,
						android.R.layout.simple_dropdown_item_1line,
						tagList.value!!.toMutableList()
					))
				
				editorMain.setText(page.content)
				editorTags.setText(page.tags as CharSequence)
				editKey = newEditKey.value
			}
			
		}
	}
	
	private fun buttonClickSave(view: View) {
		show_toast(context, "Coming soon! This hasn't been implemented yet.")
	}
}