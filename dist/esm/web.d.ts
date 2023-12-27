import { WebPlugin } from '@capacitor/core';
import type { MyPluginCallback, UsbSerialOptions, UsbSerialWriteOptions, UsbSerialPlugin, UsbSerialResponse, CallbackID } from './definitions';
export declare class UsbSerialWeb extends WebPlugin implements UsbSerialPlugin {
    usbAttachedDetached(callback: MyPluginCallback): Promise<CallbackID>;
    connectedDevices(): Promise<UsbSerialResponse>;
    openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse>;
    closeSerial(): Promise<UsbSerialResponse>;
    readSerial(): Promise<UsbSerialResponse>;
    writeSerial(data: UsbSerialWriteOptions): Promise<UsbSerialResponse>;
    registerReadCall(callback: MyPluginCallback): Promise<CallbackID>;
}
