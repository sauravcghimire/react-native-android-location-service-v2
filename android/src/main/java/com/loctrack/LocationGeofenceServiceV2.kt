package com.loctrack

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.loctrack.LocationHeadlessTask
import kotlinx.coroutines.*

class LocationGeofenceServiceV2 : Service() {

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "LocationGeofenceV2"
        private const val NOTIF_ID = 777
        private const val CHANNEL_ID = "geofence_v2_channel"
        private const val RADIUS = 100f
        private const val GEOFENCE_ID = "dynamic_geofence_v2"

        @JvmStatic var isTracking: Boolean = false

        @JvmStatic
        fun start(
            context: Context,
            onUpdate: (lat: Double, lng: Double, accuracy: Float) -> Unit
        ) {
            isTracking = true
            LocationCallbackHolder.onLocationUpdate = onUpdate

            val intent = Intent(context, LocationGeofenceServiceV2::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    // ---------------------------
    // SERVICE ENTRY POINT
    // ---------------------------
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "RESTART_GEOFENCE" -> recreateGeofence()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Service CREATED")
        isTracking = true

        geofencingClient = LocationServices.getGeofencingClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Android 14+ → MUST specify foregroundServiceType
        val notif = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }

        createInitialGeofence()
    }

    // ---------------------------
    // CREATE INITIAL GEOFENCE
    // ---------------------------
    private fun createInitialGeofence() {
        try {
            val fine = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (fine != PackageManager.PERMISSION_GRANTED &&
                coarse != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Missing permissions: cannot fetch initial location")
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {

                    // Emit first update
                    LocationCallbackHolder.onLocationUpdate?.invoke(
                        loc.latitude,
                        loc.longitude,
                        loc.accuracy
                    )
                    val headlessIntent = Intent(this, LocationHeadlessTask::class.java).apply {
                        putExtra("lat", loc.latitude)
                        putExtra("lng", loc.longitude)
                        putExtra("acc", loc.accuracy)
                    }
                    startService(headlessIntent)


                    // Register geofence
                    registerGeofence(loc.latitude, loc.longitude)

                } else {
                    Log.e(TAG, "Initial location returned NULL")
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while fetching initial location: ${e.message}")
        }
    }

    // ---------------------------
    // REGISTER NEW GEOFENCE
    // ---------------------------
    fun registerGeofence(lat: Double, lng: Double) {
        Log.d(TAG, "Registering geofence at $lat , $lng")

        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(lat, lng, RADIUS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build()

        val pendingIntent = geofencePendingIntent()

        geofencingClient.removeGeofences(pendingIntent)

        try {
            val fine = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (fine != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "FINE_LOCATION missing — cannot add geofence")
                return
            }

            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener { Log.d(TAG, "Geofence added successfully") }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add geofence: ${e.message}")
                }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException adding geofence: ${e.message}")
        }
    }

    // ---------------------------
    // RESTART FROM BROADCAST
    // ---------------------------
    private fun recreateGeofence() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                registerGeofence(loc.latitude, loc.longitude)
            } else {
                Log.e(TAG, "recreateGeofence(): lastLocation was NULL")
            }
        }
    }

    // ---------------------------
    // PENDING INTENT
    // ---------------------------
    private fun geofencePendingIntent(): android.app.PendingIntent {
        val intent = Intent(this, GeofenceReceiverV2::class.java).apply {
            action = "com.loctrack.GEOFENCE_EVENT"
        }
        return android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )
    }

    // ---------------------------
    // NOTIFICATION
    // ---------------------------
    private fun createNotification(): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Geofence Active")
            .setContentText("Tracking via geofence exit events")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    // ---------------------------
    // CLEANUP
    // ---------------------------
    override fun onDestroy() {
        super.onDestroy()
        isTracking = false
        serviceScope.cancel()
        Log.d(TAG, "Service DESTROYED")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
