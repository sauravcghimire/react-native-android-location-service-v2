package com.loctrack

import android.content.Intent
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule


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
      reactContext.stopService(Intent(reactContext, LocationGeofenceServiceV2::class.java))
  }

  @ReactMethod
  fun isLocationTrackingActive(promise: Promise) {
      promise.resolve(
          LocationServiceV2.isTracking || LocationGeofenceServiceV2.isTracking
      )
  }

  @ReactMethod
  fun startServiceWithGeofence() {
      LocationGeofenceServiceV2.start(
          context = reactContext
      ) { lat, lng, acc ->
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
}
