package com.sbrl.peppermint.ui.pageview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.PageViewModel

class PageViewFragment : Fragment() {
	
	private lateinit var pageViewModel: PageViewModel
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Each context gets it's own ViewModel instance
		pageViewModel =
			ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
		val root = inflater.inflate(R.layout.fragment_pageview, container, false)
		val textView: TextView = root.findViewById(R.id.text_pageview)
		
		Log.i("PageViewFragment", "Page name: ${pageViewModel.currentPageName.value}")
		
		textView.text = pageViewModel.currentPageName.value
		pageViewModel.currentPageName.observe(viewLifecycleOwner, Observer {
			textView.text = it
		})
		return root
	}
}