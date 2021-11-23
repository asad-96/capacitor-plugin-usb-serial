import { WebPlugin } from '@capacitor/core';

import type { MyPluginCallback, UsbSerialOptions, UsbSerialWriteOptions, UsbSerialPlugin, UsbSerialResponse, CallbackID } from './definitions';

export class UsbSerialWeb extends WebPlugin implements UsbSerialPlugin {

  async usbAttachedDetached(callback: MyPluginCallback): Promise<CallbackID> {
    return 'Usb device attached call not implemented on web yet!' + callback.name;
  }

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

  async closeSerial(): Promise<UsbSerialResponse> {
    return {
      success: false,
      error: { message: 'Usb device close not implemented on web yet!', cause: '' }
    };
  }

  async readSerial(): Promise<UsbSerialResponse> {
    return {
      success: false,
      error: { message: 'Usb serial read not implemented on web yet!', cause: '' }
    };
  }

  async writeSerial(data: UsbSerialWriteOptions): Promise<UsbSerialResponse> {
    return {
      success: false,
      error: { message: 'Usb serial write not implemented on web yet!' + data, cause: '' }
    };
  }

  async registerReadCall(callback: MyPluginCallback): Promise<CallbackID> {
    return 'Usb read call not implemented on web yet!' + callback.name;
  }
}
