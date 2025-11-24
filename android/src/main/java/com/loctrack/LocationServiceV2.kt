package com.loctrack

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationServiceV2 : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var updateInterval: Long = 5000L

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "LocationServiceV2"
        private const val NOTIFICATION_ID = 202
        private const val EXTRA_INTERVAL = "extra_interval"

        fun start(
            context: Context,
            interval: Long,
            onUpdate: (lat: Double, lng: Double, accuracy: Float) -> Unit
        ) {
            LocationCallbackHolder.onLocationUpdate = onUpdate

            val intent = Intent(context, LocationServiceV2::class.java).apply {
                putExtra(EXTRA_INTERVAL, interval)
            }

            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            LocationCallbackHolder.onLocationUpdate = null
            context.stopService(Intent(context, LocationServiceV2::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d(TAG, "Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        updateInterval = intent?.getLongExtra(EXTRA_INTERVAL, 5000L) ?: 5000L

        startForeground(NOTIFICATION_ID, createNotification())

        val request = LocationRequest.Builder(updateInterval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(updateInterval)
            .build()

        if (!hasLocationPermission()) {
            Log.e(TAG, "Missing location permission — cannot start location updates.")
            return START_NOT_STICKY
        }

        // Initialize callback BEFORE using it
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { loc ->
                    LocationCallbackHolder.onLocationUpdate?.let { callback ->
                        serviceScope.launch {
                            callback(loc.latitude, loc.longitude, loc.accuracy)
                        }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates requested")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException", e)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceJob.cancel()
        Log.d(TAG, "Service Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "location_service_v2_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking V2",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service Running")
            .setContentText("Interval: ${updateInterval / 1000}s")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
