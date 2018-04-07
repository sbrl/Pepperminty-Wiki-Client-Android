package com.sbrl.peppermint.bricks

import android.content.Context
import android.net.wifi.WifiManager

fun is_wifi_enabled(context : Context) : Boolean {
	return (context.applicationContext
		.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
}