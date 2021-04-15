package com.sbrl.peppermint.lib.helpers

class HashMapBuilder<A, B> {
	private val map: HashMap<A, B> = HashMap()
		get() = field
	
	fun put(key: A, value: B): HashMapBuilder<A, B> {
		map[key] = value
		return this
	}
}