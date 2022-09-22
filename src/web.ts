import { WebPlugin } from '@capacitor/core';

import type {
  CallbackID,
  BerialGeolocationPlugin,
  WatchPositionCallback,
} from './definitions';

export class BerialGeolocationWeb extends WebPlugin implements BerialGeolocationPlugin {
  constructor() {
    super({
      name: 'Geolocation',
      platforms: ['web'],
    });
  }
  async watchPosition(callback: WatchPositionCallback): Promise<CallbackID> {
    const id = navigator.geolocation.watchPosition(
      pos => {
        callback(pos);
      },
      err => {
        callback(null, err);
      },
    );

    return `${id}`;
  }

  async clearWatch(options: { id: string }): Promise<void> {
    window.navigator.geolocation.clearWatch(parseInt(options.id, 10));
  }
}

const BerialGeolocation = new BerialGeolocationWeb();

export { BerialGeolocation };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(BerialGeolocation);
