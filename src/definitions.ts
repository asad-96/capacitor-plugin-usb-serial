export type CallbackID = string;

export type MyPluginCallback = (message: UsbSerialResponse | null, err?: any) => void;

export interface UsbSerialPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse>;
  connectedDevices(): Promise<UsbSerialResponse>;
  registerReadCall(callback: MyPluginCallback): Promise<CallbackID>;
}

export interface UsbSerialOptions {
  deviceId?: number;
  portNum: number;
  baudRate: number;
  dataBits: number;
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
