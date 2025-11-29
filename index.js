// index.js
import { NativeModules, NativeEventEmitter } from "react-native";
import { useEffect } from "react";

const { LocationServiceModule } = NativeModules;
const emitter = new NativeEventEmitter(LocationServiceModule);

/**
 * Start foreground location service
 */
export function startLocationService(interval = 5000) {
  LocationServiceModule.startService(interval);
}

/**
 * Stop service
 */
export function stopLocationService() {
  LocationServiceModule.stopService();
}

/**
 * Returns boolean (promise) whether service is active
 */
export async function isLocationTrackingActive() {
  return LocationServiceModule.isLocationTrackingActive();
}

/**
 * Subscribe to continuous updates (wrapper)
 */
export function onLocationUpdate(callback) {
  const sub = emitter.addListener("LocationUpdate", callback);
  return () => sub.remove();
}

/**
 * Optional React Hook wrapper
 */
export function useLocationUpdates(callback) {
  useEffect(() => {
    const unsub = onLocationUpdate(callback);
    return () => unsub();
  }, [callback]);
}

export default {
  startLocationService,
  stopLocationService,
  onLocationUpdate,
  useLocationUpdates,
  isLocationTrackingActive,
};
