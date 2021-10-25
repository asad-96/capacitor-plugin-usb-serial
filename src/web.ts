import { WebPlugin } from '@capacitor/core';

import type { MyPluginCallback, UsbSerialOptions, UsbSerialPlugin, UsbSerialResponse, CallbackID } from './definitions';

export class UsbSerialWeb extends WebPlugin implements UsbSerialPlugin {

  async connectedDevices(): Promise<UsbSerialResponse> {
    return {
      success: false,
      error: { message: 'Loading connected devices not implemented on web yet!', cause: '' }
    };
  }

  async openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse> {
    return {
      success: false,
      error: { message: 'Usb device open not implemented on web yet!, BaudRate ' + options.baudRate, cause: '' }
    };
  }

  async registerReadCall(callback: MyPluginCallback): Promise<CallbackID> {
    return 'Usb read call not implemented on web yet!' + callback.name;
  }
}
