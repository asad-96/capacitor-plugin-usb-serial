import { UsbDevice } from "./UsbDevice";

export interface Device {
  device: UsbDevice;
  port: number;
  driver?: object;
}
