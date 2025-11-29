# 🚀 react-native-android-location-service-v2

A lightweight **Android-only foreground location tracking service** for React Native apps.

Perfect for:
- Delivery / Fleet tracking
- Fitness apps
- Trip logging
- Background-safe continuous GPS updates

---

## 📦 Installation

```sh
yarn add react-native-android-location-service-v2
# or
npm install react-native-android-location-service-v2
```

Autolinking works for RN 0.60+

---

## 🛠 Android Setup

Add permissions to your main app:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Register the service:

```xml
<service
  android:name="com.loctrack.LocationServiceV2"
  android:foregroundServiceType="location"
  android:exported="false" />
```

---

# 📡 Usage

## Start Tracking

```js
import LocationService from "react-native-android-location-service-v2";

LocationService.startLocationService(3000); // every 3 seconds
```

## Subscribe to updates (simple wrapper)

```js
LocationService.onLocationUpdate(loc => {
  console.log("📍", loc);
});
```

## React Hook

```js
useLocationUpdates(loc => {
  console.log("LAT:", loc.latitude);
});
```

## Stop tracking

```js
LocationService.stopLocationService();
```

## Check if tracking is active

```js
const isActive = await LocationService.isLocationTrackingActive();
```

---

# 📤 Event Payload

```json
{
  "latitude": 27.7172,
  "longitude": 85.3240,
  "accuracy": 4.5
}
```

---

# 👨‍💻 Author
**Saurav Ghimire**

# 📄 License
MIT
