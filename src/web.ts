import { WebPlugin } from '@capacitor/core';

import type { UsbSerialPlugin } from './definitions';

export class UsbSerialWeb extends WebPlugin implements UsbSerialPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
