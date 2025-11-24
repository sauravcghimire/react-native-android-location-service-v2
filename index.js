import { NativeModules, NativeEventEmitter } from "react-native";

const { LocationServiceModule } = NativeModules;
const emitter = new NativeEventEmitter(LocationServiceModule);

export function startLocationService(interval = 5000) {
  LocationServiceModule.startService(interval);
}

export function stopLocationService() {
  LocationServiceModule.stopService();
}

export function addLocationListener(callback) {
  const subscription = emitter.addListener("LocationUpdate", callback);
  return () => subscription.remove();
}

export default {
  startLocationService,
  stopLocationService,
  addLocationListener,
};
