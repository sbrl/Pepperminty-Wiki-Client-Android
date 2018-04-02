package com.sbrl.peppermint.activities

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import com.sbrl.peppermint.R
import com.sbrl.peppermint.data.PreferencesManager
import com.sbrl.peppermint.data.WikiCredentials
import khttp.responses.Response
import java.net.ConnectException
import java.net.MalformedURLException
import kotlin.concurrent.thread

class AddWiki : AppCompatActivity() {
	private val LogTag = "[activity] AddWiki"
	
    private lateinit var prefs : PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_wiki)
        
        prefs = PreferencesManager(this)
        
		// ---------------------------
		// ----- Event listeners -----
		// ---------------------------
        val addButton : Button = findViewById(R.id.add_wiki_submit)
        addButton.setOnClickListener { addWiki(it) }
		
		val urlBox : EditText = findViewById(R.id.add_wiki_url)
		urlBox.setOnFocusChangeListener { target : View, hasFocus : Boolean ->
			if(hasFocus)
				return@setOnFocusChangeListener
			
			thread(start = true) {
				checkWiki()
			}
		}
    }
    
    private fun checkWiki() {
		val url : String = (findViewById<EditText>(R.id.add_wiki_url)).text.toString()
		
		val statusResponse : Response
		try {
			statusResponse = khttp.get(
				url,
				params = mapOf(
					"action" to "status"
				),
				allowRedirects = false
			)
		} catch(error : ConnectException) {
			runOnUiThread { setWikiStatus(false, false) }
			return
		} catch(error : MalformedURLException) {
			runOnUiThread { setWikiStatus(false, false) }
			return
		}
		
		Log.i(LogTag, "Check wiki: Status code ${statusResponse.statusCode}")
		runOnUiThread {
			// If it's a non-200 code, then it (probably) requires a login
			if(statusResponse.statusCode !in 200..300)
				setWikiStatus(true, true)
			else // FUTURE: Pull out the wiki name from the status and auto-fill the box
				setWikiStatus(true, false)
		}
	}
	
	private fun setWikiStatus(canConnect : Boolean, requiresLogin : Boolean)
	{
		val statusContainer : LinearLayout = findViewById(R.id.add_wiki_status_container)
		val statusIconDisplay : ImageView = findViewById(R.id.add_wiki_status_icon)
		val statusTextDisplay : TextView = findViewById(R.id.add_wiki_status_text)
		
		statusContainer.visibility = View.VISIBLE
		if(!canConnect) {
			statusIconDisplay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_warning))
			statusIconDisplay.colorFilter = LightingColorFilter(
				ContextCompat.getColor(this, R.color.colorWarning),
				ContextCompat.getColor(this, R.color.colorWarning)
			)
			statusTextDisplay.text = getString(R.string.add_wiki_connection_failure)
		} else {
			statusIconDisplay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_ok))
			statusIconDisplay.colorFilter = LightingColorFilter(
				ContextCompat.getColor(this, R.color.colorOk),
				ContextCompat.getColor(this, R.color.colorOk)
			)
			statusTextDisplay.text = if(requiresLogin)
				getString(R.string.add_wiki_connection_ok_login)
			else
				getString(R.string.add_wiki_connection_ok)
				
		}
		
	}
    
    @SuppressLint("WrongViewCast")
    private fun addWiki(target : View) {
        // Fetch the information from the UI
        val wikiName : String = (findViewById<EditText>(R.id.add_wiki_name)).text.toString()
        val url : String = (findViewById<EditText>(R.id.add_wiki_url)).text.toString()
        val username : String = (findViewById<EditText>(R.id.add_wiki_username)).text.toString()
        val password : String = (findViewById<EditText>(R.id.add_wiki_password)).text.toString()
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
