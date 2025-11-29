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

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var callback: LocationCallback

    private var interval: Long = 5000L

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "LocationServiceV2"
        private const val NOTIF_ID = 202
        private const val EXTRA_INTERVAL = "extra_interval"
        const val EXTRA_BG_CALLBACK = "extra_bg_callback"

        @JvmStatic
        var isTracking: Boolean = false

        fun start(
            context: Context,
            interval: Long,
            onJsUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null,
            onBackgroundUpdate: ((lat: Double, lng: Double, accuracy: Float) -> Unit)? = null
        ) {
            isTracking = true

            LocationCallbackHolder.onJsLocationUpdate = onJsUpdate
            LocationCallbackHolder.onBackgroundLocationUpdate = onBackgroundUpdate

            val intent = Intent(context, LocationServiceV2::class.java).apply {
                putExtra(EXTRA_INTERVAL, interval)
            }

            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            isTracking = false
            LocationCallbackHolder.clear()
            context.stopService(Intent(context, LocationServiceV2::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        interval = intent?.getLongExtra(EXTRA_INTERVAL, 5000L) ?: 5000L

        startForeground(NOTIF_ID, notification())

        val request = LocationRequest.Builder(interval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(interval)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { loc ->
                    val lat = loc.latitude
                    val lng = loc.longitude
                    val acc = loc.accuracy

                    scope.launch {
                        // Foreground → JS listener
                        LocationCallbackHolder.onJsLocationUpdate?.invoke(lat, lng, acc)

                        // Background → Custom callback provided by JS
                        LocationCallbackHolder.onBackgroundLocationUpdate?.invoke(lat, lng, acc)
                    }
                }
            }
        }

        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(callback)
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(): Notification {
        val id = "loc_v2_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id,
                "Location Tracking V2",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, id)
            .setContentTitle("Location Tracking Active")
            .setContentText("Interval ${interval / 1000}s")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }
}
