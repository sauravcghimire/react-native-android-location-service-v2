declare module "react-native-android-location-service-v2" {
  export function startLocationService(interval: number): void;
  export function stopLocationService(): void;
  export function isLocationTrackingActive(): Promise<boolean>;

  export function addLocationListener(
    callback: (data: {
      latitude: number;
      longitude: number;
      accuracy: number;
    }) => void
  ): () => void;

  const _default: {
    startLocationService: typeof startLocationService;
    stopLocationService: typeof stopLocationService;
    addLocationListener: typeof addLocationListener;
    isLocationTrackingActive: typeof isLocationTrackingActive;
  };

  export default _default;
}
