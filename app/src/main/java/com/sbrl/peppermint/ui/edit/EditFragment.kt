package com.sbrl.peppermint.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R

class EditFragment : Fragment() {
	
	private lateinit var editViewModel: EditViewModel
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		editViewModel =
			ViewModelProvider(this)[EditViewModel::class.java]
		val root = inflater.inflate(R.layout.fragment_edit, container, false)
		val textView: TextView = root.findViewById(R.id.text_edit)
		editViewModel.text.observe(viewLifecycleOwner, Observer {
			textView.text = it
		})
		return root
	}
}