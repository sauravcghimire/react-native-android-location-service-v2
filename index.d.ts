declare module "react-native-android-location-service-v2" {
  export function startLocationService(interval: number): void;

  export function startLocationServiceWithCallback(
    interval: number,
    callback: (lat: number, lng: number, accuracy: number) => void
  ): void;

  export function stopLocationService(): void;

  export function onLocationUpdate(
    listener: (loc: {
      latitude: number;
      longitude: number;
      accuracy: number;
    }) => void
  ): () => void;

  export function isLocationTrackingActive(): Promise<boolean>;

  const _default: {
    startLocationService: typeof startLocationService;
    startLocationServiceWithCallback: typeof startLocationServiceWithCallback;
    stopLocationService: typeof stopLocationService;
    onLocationUpdate: typeof onLocationUpdate;
    isLocationTrackingActive: typeof isLocationTrackingActive;
  };

  export default _default;
}
