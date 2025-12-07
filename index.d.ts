declare module "react-native-android-location-service-v2" {
  export interface LocationData {
    latitude: number;
    longitude: number;
    accuracy: number;
  }

  /**
   * Start GPS continuous tracking
   */
  export function startLocationService(interval: number): void;

  /**
   * Start geofence-only tracking
   */
  export function startLocationServiceWithGeofence(): void;

  /**
   * Stop whichever service is running
   */
  export function stopLocationService(): void;

  /**
   * Returns boolean whether service is active
   */
  export function isLocationTrackingActive(): Promise<boolean>;

  /**
   * Subscribe to foreground location updates
   */
  export function onLocationUpdate(
    callback: (data: LocationData) => void
  ): () => void;

  /**
   * Register a handler for background/headless mode
   */
  export function registerBackgroundHandler(
    callback: (data: LocationData) => void
  ): void;

  /**
   * Internal headless task executor (exposed for AppRegistry)
   */
  export const __backgroundHandler: (data: LocationData) => Promise<void>;

  export function useLocationUpdates(
    callback: (data: LocationData) => void
  ): void;

  const _default: {
    startLocationService: typeof startLocationService;
    startLocationServiceWithGeofence: typeof startLocationServiceWithGeofence;
    stopLocationService: typeof stopLocationService;
    onLocationUpdate: typeof onLocationUpdate;
    useLocationUpdates: typeof useLocationUpdates;
    isLocationTrackingActive: typeof isLocationTrackingActive;
    registerBackgroundHandler: typeof registerBackgroundHandler;
    __backgroundHandler: typeof __backgroundHandler;
  };

  export default _default;
}
