package com.loctrack

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class LocationModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName() = "LocationServiceModule"

  /** Existing API — keeps backward compatibility */
  @ReactMethod
  fun startService(interval: Int) {
    LocationServiceV2.start(
      context = reactContext,
      interval = interval.toLong(),
      onJsUpdate = { lat, lng, acc ->
        val params = Arguments.createMap().apply {
          putDouble("latitude", lat)
          putDouble("longitude", lng)
          putDouble("accuracy", acc.toDouble())
        }

        reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("LocationUpdate", params)
      }
    )
  }

  /** New API → supports background callback */
  @ReactMethod
  fun startServiceWithCallback(interval: Int, jsFunction: Callback) {
    LocationServiceV2.start(
      context = reactContext,
      interval = interval.toLong(),
      onJsUpdate = { lat, lng, acc ->
        val params = Arguments.createMap().apply {
          putDouble("latitude", lat)
          putDouble("longitude", lng)
          putDouble("accuracy", acc.toDouble())
        }

        reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("LocationUpdate", params)
      },
      onBackgroundUpdate = { lat, lng, acc ->
        // Call user-defined background function
        jsFunction.invoke(lat, lng, acc)
      }
    )
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
