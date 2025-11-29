import { NativeModules, NativeEventEmitter } from "react-native";

const { LocationServiceModule } = NativeModules;
const emitter = new NativeEventEmitter(LocationServiceModule);

/** Old API — backward compatible */
export function startLocationService(interval = 5000) {
  LocationServiceModule.startService(interval);
}

/** New API — supports background callback */
export function startLocationServiceWithCallback(interval, callback) {
  LocationServiceModule.startServiceWithCallback(interval, callback);
}

/** Subscribe to foreground JS updates */
export function onLocationUpdate(listener) {
  const sub = emitter.addListener("LocationUpdate", listener);
  return () => sub.remove();
}

export function stopLocationService() {
  LocationServiceModule.stopService();
}

export async function isLocationTrackingActive() {
  return await LocationServiceModule.isLocationTrackingActive();
}

export default {
  startLocationService,
  startLocationServiceWithCallback,
  stopLocationService,
  onLocationUpdate,
  isLocationTrackingActive,
};
