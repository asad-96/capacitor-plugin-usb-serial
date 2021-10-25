import { ChangeDetectorRef, Component } from '@angular/core';
import { LoadingController, ToastController } from '@ionic/angular';
import { UsbSerial, UsbSerialOptions, UsbSerialResponse } from "usb-serial-plugin";
import { ToastService } from '../core/toast/toast.service';
import { Device } from '../shared/modal/Device';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {
  devices: Array<Device>;
  usbserialResponse: UsbSerialResponse;
  readData: string = "";
  readError: object;

  constructor(
    private loadingController: LoadingController,
    private changeRef: ChangeDetectorRef,
    private toastSvc: ToastService
  ) {}

  ionViewWillEnter() {
    this.loadUsbDevices();
  }

  private async loadUsbDevices() {
    const loading = await this.loadingController.create({
      message: 'Loading Devices...',
      duration: 1000
    });
    await loading.present();
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
      console.log('ready to register the call');
      UsbSerial.registerReadCall((response: UsbSerialResponse) => {
        console.log('read call response: ', response);
        this.usbserialResponse = response;
         if (response.success && response.data) {
            this.readData += response.data;
            this.toastSvc.presentToast(response.data, 500);
         } else {
            this.readError = response.error;
            this.toastSvc.presentToast(response.error.toString(), 1000);
         }
         this.changeRef.detectChanges();
      });
    }
  }
}
