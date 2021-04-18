package com.sbrl.peppermint.ui.addwiki

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
	val usernameError: Int? = null,
	val passwordError: Int? = null,
	val isDataValid: Boolean = false
)