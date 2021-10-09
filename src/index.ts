import { registerPlugin } from '@capacitor/core';

import type { UsbSerialPlugin } from './definitions';

const UsbSerial = registerPlugin<UsbSerialPlugin>('UsbSerial', {
  web: () => import('./web').then(m => new m.UsbSerialWeb()),
});

export * from './definitions';
export { UsbSerial };
