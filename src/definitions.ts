export type CallbackID = string;

export type MyPluginCallback = (data: UsbSerialResponse) => void;

export interface UsbSerialPlugin {
  openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse>;
  connectedDevices(): Promise<UsbSerialResponse>;
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
