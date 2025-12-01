# react-native-android-location-service-v2

A lightweight **Android-only foreground + background + killed-state location tracking service** for React Native apps.

Perfect for:
- Delivery / Fleet tracking  
- Fitness apps  
- Trip logging  
- Background-safe continuous GPS updates (even when the app is swiped away)  
- Custom native logic on every location update  

---

## Installation
```
yarn add react-native-android-location-service-v2  
or  
npm install react-native-android-location-service-v2

Autolinking works for RN 0.60+.
```

---

## Android Setup

Add required permissions to your main AndroidManifest.xml:
```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Register the foreground service:
```
<service
  android:name="com.loctrack.LocationServiceV2"
  android:foregroundServiceType="location"
  android:exported="false" />
```
---

# 📡 Usage

## Start Tracking
```
import LocationService from "react-native-android-location-service-v2";

LocationService.startLocationService(3000); // update every 3 seconds
```
---

## Subscribe to Location Updates (Foreground JS)
```
const unsubscribe = LocationService.onLocationUpdate(loc => {
  console.log("📍 Location:", loc);
});
// later…
unsubscribe();
```
---

## React Hook Example
```
useEffect(() => {
  const unsub = LocationService.onLocationUpdate(loc => {
    console.log("LAT:", loc.latitude);
  });
  return unsub;
}, []);
```
---

## Stop Tracking
```
LocationService.stopLocationService();
```
---

## Check if Tracking Is Active
```
const active = await LocationService.isLocationTrackingActive();
console.log("Tracking?", active);
```
---

# Location Event Payload
```
{
  "latitude": 27.7172,
  "longitude": 85.3240,
  "accuracy": 4.5
}
```
---

# Native Background Callback (Runs When App Is Killed)

When the app is killed, JS stops.  
But the library continues tracking and allows **custom native Kotlin code** to run on every location update.

Use this for:
- Storing locations in SharedPreferences  
- Uploading to server  
- Writing to a local database  
- Triggering geofences  
- Logging debug information  

---

## Step 1 — Add background callback inside MainApplication.kt

Open:
```
android/app/src/main/java/<your-package>/MainApplication.kt
```
Add:
```
import com.loctrack.LocationBackgroundHandler
import android.util.Log
import android.content.Context

override fun onCreate() {
    super.onCreate()

    // This runs even when the app is KILLED
    LocationBackgroundHandler.onLocationUpdate = { context, lat, lng, acc ->

        val prefs = context.getSharedPreferences("bg_locations", Context.MODE_PRIVATE)

        val entry = "LAT=$lat LNG=$lng ACC=$acc TIME=${System.currentTimeMillis()}"

        prefs.edit()
            .putString("last_location", entry)
            .apply()

        Log.d("BG_CALLBACK", "Background location: $entry")
    }
}
```
---

## Optional — Override in MainActivity.kt
```
LocationBackgroundHandler.onLocationUpdate = { context, lat, lng, acc ->
    Log.d("MAIN_ACTIVITY_CALLBACK", "Got location: $lat, $lng (±$acc)")
}
```
---

# API Reference

| Method | Description |
|--------|-------------|
| startLocationService(intervalMs) | Starts continuous GPS tracking |
| stopLocationService() | Stops the tracker |
| isLocationTrackingActive() | Returns true/false |
| onLocationUpdate(cb) | JS listener for foreground updates |

---

# Important Notes

- JS listeners do **not** run when the app is killed  
  → Use the **Native Background Callback**
- Android 10+ requires ACCESS_BACKGROUND_LOCATION  
- Heavy background work should run off the main thread  
- Foreground service notification is mandatory by Android rules  

---

# Author  
Saurav Ghimire

# License  
MIT
