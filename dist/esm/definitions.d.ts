export declare type CallbackID = string;
export declare type MyPluginCallback = (data: UsbSerialResponse) => void;
export interface UsbSerialPlugin {
    usbAttachedDetached(callback: MyPluginCallback): Promise<CallbackID>;
    connectedDevices(): Promise<UsbSerialResponse>;
    openSerial(options: UsbSerialOptions): Promise<UsbSerialResponse>;
    closeSerial(): Promise<UsbSerialResponse>;
    readSerial(): Promise<UsbSerialResponse>;
    writeSerial(data: UsbSerialWriteOptions): Promise<UsbSerialResponse>;
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
export interface UsbSerialWriteOptions {
    data: string;
}
export interface UsbSerialResponse {
    success: boolean;
    error?: UsbSerialError;
    data?: any;
}
export interface UsbSerialError {
    message: string;
    cause: string;
}
