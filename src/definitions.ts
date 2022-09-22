declare module '@capacitor/core' {
  interface PluginRegistry {
    BerialGeolocation: BerialGeolocationPlugin;
  }
}

export interface BerialGeolocationPlugin {

  /**
   * Set up a watch for location changes. Note that watching for location changes
   * can consume a large amount of energy. Be smart about listening only when you need to.
   *
   * @since 1.0.0
   */
  watchPosition(callback: WatchPositionCallback): Promise<CallbackID>;

  /**
   * Clear a given watch
   *
   * @since 1.0.0
   */
  clearWatch(options: ClearWatchOptions): Promise<void>;
}

export type CallbackID = string;

export interface ClearWatchOptions {
  id: CallbackID;
}

export interface Position {
  /**
   * Creation timestamp for coords
   *
   * @since 1.0.0
   */
  timestamp: number;

  /**
   * The GPS coordinates along with the accuracy of the data
   *
   * @since 1.0.0
   */
  coords: {
    /**
     * Latitude in decimal degrees
     *
     * @since 1.0.0
     */
    latitude: number;

    /**
     * longitude in decimal degrees
     *
     * @since 1.0.0
     */
    longitude: number;

    /**
     * Accuracy level of the latitude and longitude coordinates in meters
     *
     * @since 1.0.0
     */
    accuracy: number;

    /**
     * Accuracy level of the altitude coordinate in meters, if available.
     *
     * Available on all iOS versions and on Android 8.0+.
     *
     * @since 1.0.0
     */
    altitudeAccuracy: number | null | undefined;

    /**
     * The altitude the user is at (if available)
     *
     * @since 1.0.0
     */
    altitude: number | null;

    /**
     * The speed the user is traveling (if available)
     *
     * @since 1.0.0
     */
    speed: number | null;

    /**
     * The heading the user is facing (if available)
     *
     * @since 1.0.0
     */
    heading: number | null;
  };
}

export type WatchPositionCallback = (
  position: Position | null,
  err?: any,
) => void;