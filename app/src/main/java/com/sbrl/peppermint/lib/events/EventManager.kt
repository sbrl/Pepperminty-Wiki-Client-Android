package com.sbrl.peppermint.lib.events

import android.util.Log
import kotlin.Exception

/**
 * Represents a *single* event.
 * The interface is a mix between the C♯ and Javascript event systems.
 *
 */
class EventManager<EventSource, EventArgs>(val name: String) {
	/**
	 * A list of the current event listeners.
	 */
	private val listeners: MutableList<(source: EventSource, args: EventArgs) -> Unit> = mutableListOf()
	
	private val listenerCountWarn: Int = 25
	
	// Virtual (/ computed) property
	private val tag: String
		get() = "EventManager/$name"
	
	/**
	 * Adds a new event listener to this event manager.
	 * @param fn: The function to call. Will be called with 2 parameters: The event source (i.e. who threw the event), and the event args (i.e. the arguments to the event).
	 */
	fun on(fn: (source: EventSource, args: EventArgs) -> Unit) {
		listeners.add(fn)
		
		if(listeners.size > listenerCountWarn)
			Log.w(
				tag,
				"Warning: ${listeners.size} event listeners attached, is there a leak?\n" +
					Log.getStackTraceString(Exception())
			)
	}
	
	/**
	 * Removes the given function from the list of listeners.
	 * @return Whether it was present in the list of listeners in the first place or not.
	 */
	fun off(fn: (source: EventSource, args: EventArgs) -> Unit) : Boolean {
		return listeners.remove(fn)
	}
	
	/**
	 * Deletes all the listeners from this EventManager.
	 */
	fun clear() {
		listeners.clear()
	}
	
	/**
	 * Emits an event to all the listeners currently attached to this EventManager.
	 * @param source: The caller of the event.
	 * @param args: The arguments to the event.
	 * @return Whether there were listeners present that were triggered or not.
	 */
	fun emit(source: EventSource, args: EventArgs) : Boolean {
		Log.i(tag, "Emitting to ${listeners.size} listeners")
		if(listeners.size == 0) return false
		
		for(fn in listeners)
			fn(source, args)
		
		return true
	}
}