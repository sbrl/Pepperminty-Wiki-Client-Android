package com.sbrl.peppermint.ui

/**
 * The name of the wiki the page belongs to. If omitted, the current wiki is assumed.
 * If no current wiki is selected, the default wiki is made the current wiki.
 */
const val EXTRA_WIKI_ID: String = "com.sbrl.peppermint.WIKI_NAME"

/**
 * The name of the page to talk about.
 */
const val EXTRA_PAGE_NAME: String = "com.sbrl.peppermint.PAGE_NAME"

// Return code to indicate that a new wiki was added.
const val RETURN_ADDED_WIKI: Int = 0
// Return code to indicate that the settings were updated.
const val RETURN_UPDATED_SETTINGS: Int = 1