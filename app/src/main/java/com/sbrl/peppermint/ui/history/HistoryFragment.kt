package com.sbrl.peppermint.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.edit.HistoryViewModel

class HistoryFragment : Fragment() {
	
	private lateinit var historyViewModel: HistoryViewModel
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		historyViewModel =
			ViewModelProvider(this).get(HistoryViewModel::class.java)
		val root = inflater.inflate(R.layout.fragment_history, container, false)
		val textView: TextView = root.findViewById(R.id.text_history)
		historyViewModel.text.observe(viewLifecycleOwner, Observer {
			textView.text = it
		})
		return root
	}
}
