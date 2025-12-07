import { NativeModules, NativeEventEmitter } from "react-native";

const { LocationServiceModule } = NativeModules;
const emitter = new NativeEventEmitter(LocationServiceModule);

// --------------------------------------------------
// Foreground listener storage
// --------------------------------------------------
export function startLocationService(interval = 5000) {
  LocationServiceModule.startService(interval);
}

export function startLocationServiceWithGeofence() {
  LocationServiceModule.startServiceWithGeofence();
}

export function stopLocationService() {
  LocationServiceModule.stopService();
}

export async function isLocationTrackingActive() {
  return LocationServiceModule.isLocationTrackingActive();
}

export function onLocationUpdate(callback) {
  const sub = emitter.addListener("LocationUpdate", callback);
  return () => sub.remove();
}

export function useLocationUpdates(callback) {
  useEffect(() => {
    const unsub = onLocationUpdate(callback);
    return () => unsub();
  }, [callback]);
}

// --------------------------------------------------
// BACKGROUND HANDLER STORAGE
// --------------------------------------------------
let backgroundHandler = null;

/**
 * Register a JS handler that will run in headless/background mode.
 */
export function registerBackgroundHandler(cb) {
  backgroundHandler = cb;
}

/**
 * Called by Headless JS → executes developer's callback.
 */
export const __backgroundHandler = async (data) => {
  try {
    if (backgroundHandler) {
      await backgroundHandler(data);
    }
  } catch (e) {
    console.log("Background handler error", e);
  }
};

// --------------------------------------------------
// EXPORT PUBLIC API
// --------------------------------------------------
export default {
  startLocationService,
  startLocationServiceWithGeofence,
  stopLocationService,
  onLocationUpdate,
  useLocationUpdates,
  isLocationTrackingActive,
  registerBackgroundHandler,
  __backgroundHandler,
};
