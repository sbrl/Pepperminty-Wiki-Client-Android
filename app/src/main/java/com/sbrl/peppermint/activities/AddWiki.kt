package com.sbrl.peppermint.activities

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import java.security.AccessController.getContext

import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.WikiCredentials

class AddWiki : AppCompatActivity() {
    private lateinit var prefs : PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_wiki)
        
        prefs = PreferencesManager(this)
        
        val addButton : Button = findViewById(R.id.add_wiki_submit);
        addButton.setOnClickListener { addWiki(it) };
    }
    
    @SuppressLint("WrongViewCast")
    private fun addWiki(target : View) {
        // Fetch the information from the UI
        val wikiName : String = findViewById<EditText>(R.id.add_wiki_name).text.toString()
        val url : String = findViewById<EditText>(R.id.add_wiki_url).text.toString()
        val username : String = findViewById<EditText>(R.id.add_wiki_username).text.toString()
        val password : String = findViewById<EditText>(R.id.add_wiki_password).text.toString()
        // Bundle them up into the appropriate class
        val newCredentials = WikiCredentials(url, username, password)
        
        // Make sure we haven't added this wiki already
        if(prefs.HasCredentials(wikiName))
            return
        
        // Add the wiki to our internal preferences
        prefs.AddWiki(wikiName, newCredentials)
        
        // Tell the user we've added the wiki
        Toast.makeText(this, "$wikiName added successfully", LENGTH_LONG).show()
        
        finish()
    }
}
