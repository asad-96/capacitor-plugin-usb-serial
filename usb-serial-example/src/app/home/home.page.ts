import { Component } from '@angular/core';
import { LoadingController } from '@ionic/angular';
import { UsbSerial, UsbSerialOptions, UsbSerialResponse } from "usb-serial-plugin";
import { Device } from '../shared/modal/Device';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {
  devices: Array<Device>;
  usbserialResponse: UsbSerialResponse;
  private usbserialOptions: UsbSerialOptions = { portNum: 25, baudRate: 38400, dataBits: 8 };
  readData: Uint8Array;
  readError: Object;

  constructor(
    private loadingController: LoadingController
  ) {}

  ionViewWillEnter() {
    this.loadUsbDevices();
  }

  private async loadUsbDevices() {
    const result = await UsbSerial.connectedDevices();
    if (result.success) {
      console.log("Plugin Result Data", result.data);
      this.devices = (<any> result.data).devices;
    }
    console.log("Plugin Result", result);
  }

  async retry() {
    delete this.devices;
    delete this.usbserialResponse;
    const loading = await this.loadingController.create({
      message: 'Please wait...',
      duration: 3000
    });
    await loading.present();
    this.loadUsbDevices();
  }

  async onDeviceSelected(item: Device) {
    const loading = await this.loadingController.create({
      message: 'Please wait...',
      duration: 3000
    });
    await loading.present();
    const usbSerialOptions: UsbSerialOptions = { deviceId: item.device.deviceId, portNum: item.port, baudRate: 38400, dataBits: 8 }
    this.usbserialResponse = await UsbSerial.openSerial(usbSerialOptions);
    console.log(this.usbserialResponse);
    if (this.usbserialResponse.success) {
      UsbSerial.registerReadCall(async (response: UsbSerialResponse) => {
         if (response.success && response.data) {
            this.readData = new Uint8Array(response.data);
         } else {
            this.readError = response.error;
         }
      });
    }
  }
}
