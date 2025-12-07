# 🚀 react-native-android-location-service-v2

Reliable **Android-only foreground + background + killed-state location tracking** for React Native.

Perfect for:
- Delivery / Fleet tracking  
- Fitness & route logging  
- Passive background movement detection  
- High-accuracy GPS + geofence-based tracking  
- Running JS **even when the app is killed**  

This library provides:
- 🔹 Continuous GPS tracking  
- 🔹 Geofence-driven tracking (low battery use)  
- 🔹 JS foreground listeners  
- 🔹 JS **background headless task**  
- 🔹 Simple & stable RN API  
- 🔹 Native Kotlin implementation  

---

## 📦 Installation

```
yarn add react-native-android-location-service-v2
# or
npm install react-native-android-location-service-v2
```

Autolinking works for RN 0.60+

---

## ⚙️ Android Setup

Add required permissions to your **app's AndroidManifest.xml**:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### ❗ No service or receiver declarations needed  
The library auto-registers:

- `LocationServiceV2`
- `LocationGeofenceServiceV2`
- `GeofenceReceiverV2`

Do **not** add them manually.

---

# 📡 Usage

## ▶️ Start continuous GPS tracking

```js
import LocationService from "react-native-android-location-service-v2";

LocationService.startLocationService(3000); // every 3 seconds
```

---

## ▶️ Start Geofence-based tracking (battery efficient)

```js
LocationService.startLocationServiceWithGeofence();
```

---

## 🛑 Stop tracking

```js
LocationService.stopLocationService();
```

---

## ❓ Check if tracking is active

```js
const active = await LocationService.isLocationTrackingActive();
console.log("Tracking active?", active);
```

---

# 🎧 Foreground JS Listener (runs when app is open)

```js
const unsubscribe = LocationService.onLocationUpdate(({ latitude, longitude, accuracy }) => {
  console.log("Foreground location:", latitude, longitude, accuracy);
});

// later
unsubscribe();
```

---

# 🪝 React Hook Usage

```js
useEffect(() => {
  return LocationService.onLocationUpdate(loc => {
    console.log("Hook location:", loc);
  });
}, []);
```

---

# 🛰 Background / Killed-State JS Handler (Headless Task)

Runs when:
- App is backgrounded  
- App is killed  
- Device is locked  

### 1️⃣ Register background handler once in JS (App.js or index.js)

```js
import LocationService from "react-native-android-location-service-v2";

LocationService.registerBackgroundHandler(async ({ latitude, longitude, accuracy }) => {
  console.log("📡 Background:", latitude, longitude, accuracy);

  await fetch("https://your-server.com/locations", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      lat: latitude,
      lng: longitude,
      acc: accuracy,
      timestamp: Date.now(),
    }),
  });
});
```

---

### 2️⃣ Register Headless Task in `index.js`

```js
import { AppRegistry } from "react-native";
import App from "./App";
import { name as appName } from "./app.json";
import LocationService from "react-native-android-location-service-v2";

// MUST match the native name "LocationBackgroundTask"
AppRegistry.registerHeadlessTask(
  "LocationBackgroundTask",
  () => LocationService.__backgroundHandler
);

AppRegistry.registerComponent(appName, () => App);
```

---

# 📤 Location event example

```json
{
  "latitude": 27.7172,
  "longitude": 85.3240,
  "accuracy": 4.2
}
```

---

# ⚡ Geofence Tracking Mode

The library includes a Kotlin-based geofence engine:

- Creates a geofence around the user  
- Fires when the user exits  
- Fetches fresh GPS  
- Sends update to foreground JS  
- Sends update to background JS (headless task)  
- Recreates new geofence  
- Runs forever  

Start it:

```js
LocationService.startLocationServiceWithGeofence();
```

This is **much more battery-friendly** than continuous GPS.

---

# 📘 TypeScript Definitions

```ts
declare module "react-native-android-location-service-v2" {
  export interface LocationData {
    latitude: number;
    longitude: number;
    accuracy: number;
  }

  export function startLocationService(interval: number): void;
  export function startLocationServiceWithGeofence(): void;
  export function stopLocationService(): void;
  export function isLocationTrackingActive(): Promise<boolean>;

  export function onLocationUpdate(
    cb: (data: LocationData) => void
  ): () => void;

  export function registerBackgroundHandler(
    cb: (data: LocationData) => void
  ): void;

  export function useLocationUpdates(
    cb: (data: LocationData) => void
  ): void;

  const _default: any;

  export default _default;
}
```

---

# 🧩 API Summary

| Method | Description |
|--------|-------------|
| `startLocationService(interval)` | Start GPS tracking |
| `startLocationServiceWithGeofence()` | Start geofence-driven tracking |
| `stopLocationService()` | Stop all tracking |
| `isLocationTrackingActive()` | Returns true/false |
| `onLocationUpdate(cb)` | Foreground JS listener |
| `registerBackgroundHandler(cb)` | Background JS listener (killed state) |
| `useLocationUpdates(cb)` | React hook wrapper |

---

# ⚠️ Important Notes

- Headless JS only runs on **real devices**, not emulator  
- Background tracking requires `"ACCESS_BACKGROUND_LOCATION"`  
- Android 14 requires foregroundServiceType="location" (already configured)  
- Foreground notification is required by Android OS  
- JS callbacks stop when app is killed → background handler continues  

---

# 👨‍💻 Author

**Saurav Ghimire**

---

# 📄 License

MIT
