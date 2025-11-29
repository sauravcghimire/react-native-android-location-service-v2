declare module "react-native-android-location-service-v2" {
  export interface LocationData {
    latitude: number;
    longitude: number;
    accuracy: number;
  }

  export function startLocationService(interval: number): void;
  export function stopLocationService(): void;
  export function isLocationTrackingActive(): Promise<boolean>;

  export function onLocationUpdate(
    callback: (data: LocationData) => void
  ): () => void;

  export function useLocationUpdates(
    callback: (data: LocationData) => void
  ): void;

  const _default: {
    startLocationService: typeof startLocationService;
    stopLocationService: typeof stopLocationService;
    onLocationUpdate: typeof onLocationUpdate;
    useLocationUpdates: typeof useLocationUpdates;
    isLocationTrackingActive: typeof isLocationTrackingActive;
  };

  export default _default;
}
