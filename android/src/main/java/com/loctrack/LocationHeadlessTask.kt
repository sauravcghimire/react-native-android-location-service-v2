package com.loctrack

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.bridge.Arguments

class LocationHeadlessTask : HeadlessJsTaskService() {

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        if (intent == null) return null

        val data = Arguments.createMap().apply {
            putDouble("latitude", intent.getDoubleExtra("lat", 0.0))
            putDouble("longitude", intent.getDoubleExtra("lng", 0.0))
            putDouble("accuracy", intent.getFloatExtra("acc", 0f).toDouble())
        }

        return HeadlessJsTaskConfig(
            "LocationBackgroundTask",
            data,
            0,
            true
        )
    }
}
