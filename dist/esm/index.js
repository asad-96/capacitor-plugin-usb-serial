import { registerPlugin } from '@capacitor/core';
const UsbSerial = registerPlugin('UsbSerial', {
    web: () => import('./web').then(m => new m.UsbSerialWeb()),
});
export * from './definitions';
export { UsbSerial };
//# sourceMappingURL=index.js.map