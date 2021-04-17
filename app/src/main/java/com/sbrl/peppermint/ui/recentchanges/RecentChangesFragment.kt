package com.sbrl.peppermint.ui.recentchanges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R

class RecentChangesFragment : Fragment() {

    private lateinit var recentChangesViewModel: RecentChangesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        recentChangesViewModel =
                ViewModelProvider(this).get(RecentChangesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_recentchanges, container, false)
        val textView: TextView = root.findViewById(R.id.text_recentchanges)
        recentChangesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}