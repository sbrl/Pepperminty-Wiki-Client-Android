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
import org.json.JSONException
import org.json.JSONObject
import java.net.ConnectException
import java.net.MalformedURLException
import kotlin.concurrent.thread

enum class TestConnectionStatus {
	Ok,
	RequiresLogin,
	Error
}

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
		} catch(error : Exception) {
			Log.e(LogTag, error.toString())
			error.printStackTrace()
			runOnUiThread { setWikiStatus(TestConnectionStatus.Error, error.message) }
			return
		}
		
		if(statusResponse.statusCode == 406) {
			runOnUiThread { setWikiStatus(TestConnectionStatus.Error, getString(R.string.add_wiki_old_version)) }
			return
		}
		
		Log.i(LogTag, "Check wiki: Status code ${statusResponse.statusCode}")
		// Check for the presence of the x-login-required header
		if(statusResponse.headers.contains("x-login-required") &&
			statusResponse.headers["x-login-required"] == "yes")
			runOnUiThread { setWikiStatus(TestConnectionStatus.RequiresLogin) }
		
		// There's no login header, but it might still be a wiki
		
		// Try parsing the response as JSON
		try {
			val statusObj = JSONObject(statusResponse.text)
			if(!statusObj.has("status")) {
				runOnUiThread { setWikiStatus(TestConnectionStatus.Error, getString(R.string.add_wiki_no_status_param))}
				return
			}
			if(statusObj.get("status") == "ok") {
				runOnUiThread { setWikiStatus(TestConnectionStatus.Ok) }
				return
			} else {
				runOnUiThread { setWikiStatus(TestConnectionStatus.Error, getString(R.string.add_wiki_invalid_status).replace("{0}", statusObj.getString("status")))}
			}
		} catch(error : JSONException) {
			runOnUiThread { setWikiStatus(TestConnectionStatus.Error, getString(R.string.add_wiki_invalid_status_response)) }
		}
		
		
	}
	
	private fun setWikiStatus(status: TestConnectionStatus, errorMessage : String? = null)
	{
		val statusContainer : LinearLayout = findViewById(R.id.add_wiki_status_container)
		val statusIconDisplay : ImageView = findViewById(R.id.add_wiki_status_icon)
		val statusTextDisplay : TextView = findViewById(R.id.add_wiki_status_text)
		
		statusContainer.visibility = View.VISIBLE
		if(status == TestConnectionStatus.Error) {
			statusIconDisplay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_warning))
			statusIconDisplay.colorFilter = LightingColorFilter(
				ContextCompat.getColor(this, R.color.colorWarning),
				ContextCompat.getColor(this, R.color.colorWarning)
			)
			statusTextDisplay.text = getString(R.string.add_wiki_connection_failure)
				.replace("{0}", errorMessage ?: "")
		} else {
			statusIconDisplay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_ok))
			statusIconDisplay.colorFilter = LightingColorFilter(
				ContextCompat.getColor(this, R.color.colorOk),
				ContextCompat.getColor(this, R.color.colorOk)
			)
			statusTextDisplay.text = if(status == TestConnectionStatus.RequiresLogin)
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
