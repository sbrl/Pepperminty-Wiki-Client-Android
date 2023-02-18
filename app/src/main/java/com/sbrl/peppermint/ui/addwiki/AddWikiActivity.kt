package com.sbrl.peppermint.ui.addwiki

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sbrl.peppermint.R
import com.sbrl.peppermint.lib.wiki_api.ConnectionStatus
import com.sbrl.peppermint.lib.wiki_api.Wiki
import com.sbrl.peppermint.ui.EXTRA_SWAP_MAIN
import com.sbrl.peppermint.ui.MainActivity
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
	private lateinit var buttonTest: Button
	private lateinit var buttonLogin: Button
	
	/**
	 * If set to true, then instead of exiting as normal and going back 1 in the activity stack,
	 * we instead swap out for the MainActivity.
	 * Used on first run, to avoid letting MainActivity continue and crash when we don't have any
	 * wikis initialised.
	 */
	private var swapToMainOnExit: Boolean = false
	
	override fun onCreate(savedInstanceState: Bundle?) {
		
		// 1: Preamble
		super.onCreate(savedInstanceState)
		
		setContentView(R.layout.activity_add_wiki)
		
		// 2: Find views
		labelStatus = findViewById(R.id.connection_status_display)
		textEndpoint = findViewById(R.id.endpoint)
		textDisplayName = findViewById(R.id.display_name)
		textUsername = findViewById(R.id.username)
		textPassword = findViewById(R.id.password)
		buttonTest = findViewById(R.id.test_connection)
		buttonLogin = findViewById(R.id.add_wiki)
		progressBarLoading = findViewById(R.id.loading)
		
		// 3: Fetch the ViewModel
		wikiViewModel = ViewModelProvider(this)[WikiViewModel::class.java]
		wikiViewModel.init(this)
		addWikiManager = AddWikiManager(this, wikiViewModel)
		
		// 4: Attach events
		textEndpoint.setOnFocusChangeListener { _target : View, hasFocus : Boolean ->
			if(hasFocus) return@setOnFocusChangeListener
			checkDetails()
		}
		textPassword.setOnFocusChangeListener { _target: View, hasFocus: Boolean ->
			if (hasFocus) return@setOnFocusChangeListener
			checkDetails()
		}
		buttonTest.setOnClickListener {
			checkDetails()
		}
		buttonLogin.setOnClickListener {
			doAddWiki()
		}
		
		// We don't live update on the username / password, because we could leak data about the username / password combo over the Internet
		
		// 5: Read Intent
		if(intent.hasExtra(EXTRA_SWAP_MAIN))
			swapToMainOnExit = intent.getBooleanExtra(EXTRA_SWAP_MAIN, false)
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
		buttonLogin.isEnabled = false
		
		buttonLogin.isEnabled = when(status) {
			ConnectionStatus.Ok, ConnectionStatus.CredentialsRequired -> true
			else -> false
		}
		
		showStatus(ConnectionStatus.toMessage(status))
	}
	
	/**
	 * Reads the data from the UI and adds a wiki to the WikiManager.
	 */
	private fun doAddWiki() {
		Log.i("AddWikiActivity", "doAddWiki start")
		progressBarLoading.visibility = View.VISIBLE
		
		val endpoint: String = textEndpoint.text.toString()
		val displayName: String = textDisplayName.text.toString()
		val username: String = textUsername.text.toString()
		val password: String = textPassword.text.toString()
		
		val wiki: Wiki = addWikiManager.createWiki(
			displayName,
			endpoint,
			username,
			password
		)
		
		thread {
			val status = wiki.connectionStatus()
			
			runOnUiThread {
				Log.i("AddWikiActivity", "doAddWiki end, connection status = $status")
				
				updateUI(status)
				if(status !== ConnectionStatus.Ok) {
					return@runOnUiThread
				}
				
				// Add the wiki to the WikiManager, and then hide the progress bar
				addWikiManager.addWiki(wiki)
				
				progressBarLoading.visibility = View.GONE
				
				// We're done here - close the add wiki activity
				if(swapToMainOnExit) {
					val intent = Intent(this, MainActivity::class.java).apply {
						flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
					}
					startActivity(intent)
				}
				else
					finish()
			}
		}
	}
	
	private fun showStatus(@StringRes stringId: Int) {
		labelStatus.visibility = View.VISIBLE
		labelStatus.text = getString(stringId)
		//show_toast(applicationContext, errorString)
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