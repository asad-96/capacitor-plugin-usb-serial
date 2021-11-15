export type CallbackID = string;

export type MyPluginCallback = (data: UsbSerialResponse) => void;

export interface UsbSerialPlugin {
  usbDeviceAttached(callback: MyPluginCallback): Promise<CallbackID>;
  usbDeviceDetached(callback: MyPluginCallback): Promise<CallbackID>;
  connectedDevices(): Promise<UsbSerialResponse>;
  openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse>;
  closeSerial(): Promise<UsbSerialResponse>;
  readSerial(): Promise<UsbSerialResponse>;
  writeSerial(): Promise<UsbSerialResponse>;
  registerReadCall(callback: MyPluginCallback): Promise<CallbackID>;
}

export interface UsbSerialOptions {
  deviceId: number;
  portNum: number;
  baudRate?: number;
  dataBits?: number;
  stopBits?: number;
  parity?: number;
  dtr?: boolean;
  rts?: boolean;
  sleepOnPause?: boolean;
}

export interface UsbSerialResponse {
  success: boolean;
  error?: object;
  data?: any;
}

export interface UsbSerialError {
  message: string;
  cause: object;
}
