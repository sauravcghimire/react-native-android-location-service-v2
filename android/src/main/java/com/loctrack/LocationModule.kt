package com.loctrack

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class LocationModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "LocationServiceModule"

    /**
     * Start the service
     */
    @ReactMethod
    fun startService(interval: Int) {
        LocationServiceV2.start(
            context = reactContext,
            interval = interval.toLong()
        ) { lat, lng, acc ->
            sendLocationEvent(lat, lng, acc)
        }
    }

    /**
     * Stop the service
     */
    @ReactMethod
    fun stopService() {
        LocationServiceV2.stop(reactContext)
    }

    @ReactMethod
    fun isLocationTrackingActive(promise: Promise) {
        promise.resolve(LocationServiceV2.isTracking)
    }

    /**
     * Send event to JS listener
     */
    private fun sendLocationEvent(lat: Double, lng: Double, acc: Float) {
        val params = Arguments.createMap().apply {
            putDouble("latitude", lat)
            putDouble("longitude", lng)
            putDouble("accuracy", acc.toDouble())
        }

        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("LocationUpdate", params)
    }
}
