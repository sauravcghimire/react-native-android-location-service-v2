package com.loctrack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.loctrack.LocationHeadlessTask
import kotlinx.coroutines.*

class GeofenceReceiverV2 : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != "com.loctrack.GEOFENCE_EVENT") {
            Log.e("GeofenceReceiverV2", "Invalid intent action: ${intent.action}")
            return
        }

        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.e("GeofenceReceiverV2", "GeofencingEvent is NULL")
            return
        }

        if (event.geofenceTransition != com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT) {
            return
        }

        Log.d("GeofenceReceiverV2", "⚠️ Geofence EXIT triggered")

        val fused = LocationServices.getFusedLocationProviderClient(context)

        // Run network/location in background so we don't block broadcast thread
        scope.launch {

            try {
                val fine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                val coarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)

                if (fine != PackageManager.PERMISSION_GRANTED &&
                    coarse != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("GeofenceReceiverV2", "Missing permissions — cannot fetch location")
                    return@launch
                }

                fused.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {

                        // Emit update to JS/native background handler
                        LocationCallbackHolder.onLocationUpdate?.invoke(
                            loc.latitude, loc.longitude, loc.accuracy
                        )
                        val headlessIntent = Intent(context, LocationHeadlessTask::class.java).apply {
                            putExtra("lat", loc.latitude)
                            putExtra("lng", loc.longitude)
                            putExtra("acc", loc.accuracy)
                        }
                        context.startService(headlessIntent)

                        Log.d("GeofenceReceiverV2", "Sending RESTART_GEOFENCE to service")

                        // ⭐ Correct way to restart geofence on Android 14+
                        val restartIntent = Intent(context, LocationGeofenceServiceV2::class.java)
                        restartIntent.action = "RESTART_GEOFENCE"

                        ContextCompat.startForegroundService(context, restartIntent)

                    } else {
                        Log.e("GeofenceReceiverV2", "lastLocation == NULL")
                    }
                }

            } catch (e: SecurityException) {
                Log.e("GeofenceReceiverV2", "SecurityException: ${e.message}")
            }
        }
    }
}
