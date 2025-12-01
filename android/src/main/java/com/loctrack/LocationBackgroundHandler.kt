package com.loctrack



/**
 * For developers: Add your own background handling logic here.
 *
 * This callback is executed by LocationServiceV2
 * even when the React Native JS engine is not alive.
 */
object LocationBackgroundHandler {
    var onLocationUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null
}
