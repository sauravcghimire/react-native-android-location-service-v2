package com.loctrack

/**
 * Holds callbacks:
 * 1. JS EventEmitter callback (existing behavior) → foreground only
 * 2. Background callback (new) → runs even when app is killed
 */

object LocationCallbackHolder {

    /** Foreground JS listener */
    var onJsLocationUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null

    /** Background callback passed from JS (string command) */
    var onBackgroundLocationUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null

    /** Clears both */
    fun clear() {
        onJsLocationUpdate = null
        onBackgroundLocationUpdate = null
    }
}
