package com.sbrl.peppermint.ui.pageview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.ui.home.PageViewViewModel

class PageViewFragment : Fragment() {

  private lateinit var pageViewViewModel: PageViewViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    pageViewViewModel =
            ViewModelProvider(this).get(PageViewViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_pageview, container, false)
    val textView: TextView = root.findViewById(R.id.text_home)
    pageViewViewModel.text.observe(viewLifecycleOwner, Observer {
      textView.text = it
    })
    return root
  }
}