package com.sbrl.peppermint.ui.addwiki

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.wiki_api.ConnectionStatus
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.ui.WikiViewModel
import kotlin.concurrent.thread

class AddWikiActivity : AppCompatActivity() {
	
	private lateinit var wikiViewModel: WikiViewModel
	private lateinit var addWikiManager: AddWikiManager
	
	private lateinit var labelStatus: TextView
	private lateinit var textEndpoint: EditText
	private lateinit var textDisplayName: EditText
	private lateinit var textUsername: EditText
	private lateinit var textPassword: EditText
	private lateinit var progressBarLoading: ProgressBar
	private lateinit var buttonLogin: Button
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContentView(R.layout.activity_add_wiki)
		
		labelStatus = findViewById(R.id.connection_status_display)
		textEndpoint = findViewById(R.id.endpoint)
		textDisplayName = findViewById(R.id.display_name)
		textUsername = findViewById(R.id.username)
		textPassword = findViewById(R.id.password)
		buttonLogin = findViewById(R.id.add_wiki)
		progressBarLoading = findViewById(R.id.loading)
		
		wikiViewModel = ViewModelProvider(this).get(WikiViewModel::class.java)
		wikiViewModel.init(this)
		addWikiManager = AddWikiManager(this, wikiViewModel)
		
		textEndpoint.setOnFocusChangeListener { _target : View, hasFocus : Boolean ->
			if(hasFocus) return@setOnFocusChangeListener
			checkDetails()
		}
		// We don't live update on the username / password, because we could leak data about the username / password combo over the Internet
	}
	
	/**
	 * Reads the wiki details from the UI and checks the connection thereto.
	 */
	private fun checkDetails() {
		progressBarLoading.visibility = View.VISIBLE
		
		val endpoint: String = textEndpoint.text.toString()
		val username: String = textUsername.text.toString()
		val password: String = textPassword.text.toString()
		
		thread {
			val status = if(username.isEmpty() || password.isEmpty())
				addWikiManager.testSettings(endpoint)
			else
				addWikiManager.testSettings(endpoint, username, password)
			
			runOnUiThread {
				progressBarLoading.visibility = View.GONE
				updateUI(status)
			}
		}
	}
	
	/**
	 * Updates the UI based on a given connection status.
	 * @param status: The ConnectionStatus to use to decide what the UI should look like.
	 */
	private fun updateUI(status: ConnectionStatus) {
		showStatus(when(status) {
			ConnectionStatus.Ok -> {
				buttonLogin.isEnabled = true
				R.string.connection_ok
			}
			ConnectionStatus.Untested,
			ConnectionStatus.ConnectionFailed ->
				R.string.connection_failed
			ConnectionStatus.CredentialsIncorrect ->
				R.string.login_failed
			ConnectionStatus.CredentialsRequired ->
				R.string.login_required
		})
	}
	
	/**
	 * Reads the data from the UI and adds a wiki to the WikiManager.
	 */
	private fun doAddWiki() {
		progressBarLoading.visibility = View.VISIBLE
		
		val endpoint: String = textEndpoint.text.toString()
		val displayName: String = textDisplayName.text.toString()
		val username: String = textUsername.text.toString()
		val password: String = textPassword.text.toString()
		
		val wiki: Wiki = addWikiManager.createWiki(
			endpoint,
			displayName,
			username,
			password
		)
		
		thread {
			val status = wiki.connectionStatus()
			
			runOnUiThread {
				if(status !== ConnectionStatus.Ok) {
					updateUI(status)
					return@runOnUiThread
				}
				
				// Add the wiki to the WikiManager, and then hide the progress bar
				addWikiManager.addWiki(wiki)
				
				progressBarLoading.visibility = View.GONE
				
				// We're done here - close the add wiki activity
				finish()
			}
		}
	}
	
	private fun showStatus(@StringRes stringId: Int) {
		labelStatus.visibility = View.VISIBLE
		labelStatus.text = getString(stringId)
		//Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
	}
	
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
	this.addTextChangedListener(object : TextWatcher {
		override fun afterTextChanged(editable: Editable?) {
			afterTextChanged.invoke(editable.toString())
		}
		
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
		
		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
	})
}