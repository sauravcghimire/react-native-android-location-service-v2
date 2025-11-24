package com.loctrack

/**
 * Holds the callback function that React Native sets when starting the location service.
 * This allows the Android service to invoke a JS callback via events.
 */
object LocationCallbackHolder {
    var onLocationUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null
}
