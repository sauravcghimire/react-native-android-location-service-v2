package com.loctrack

import com.facebook.react.bridge.*

class LocationModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName() = "LocationServiceModule"

  @ReactMethod
  fun startService(interval: Int) {
    LocationServiceV2.start(
      context = reactContext,
      interval = interval.toLong()
    ) { lat, lng, acc ->
      // Emit location event
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

  @ReactMethod
  fun stopService() {
    LocationServiceV2.stop(reactContext)
  }

  @ReactMethod
  fun isLocationTrackingActive(promise: Promise) {
    promise.resolve(LocationServiceV2.isTracking)
  }
}
