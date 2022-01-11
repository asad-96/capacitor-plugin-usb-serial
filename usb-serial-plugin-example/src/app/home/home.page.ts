import { ChangeDetectorRef, Component } from '@angular/core';
import { Clipboard } from '@capacitor/clipboard';
import { LoadingController } from '@ionic/angular';
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
  sendCmnd: string = "";

  constructor(
    private loadingController: LoadingController,
    private changeRef: ChangeDetectorRef,
    private toastSvc: ToastService
  ) {}

  ionViewDidEnter() {
    UsbSerial.usbAttachedDetached((response: UsbSerialResponse) => {
      if (response.success && response.data) {
        if (!this.usbserialResponse) {
          if (response.data == 'NEW_USB_DEVICE_ATTACHED') {
            this.toastSvc.presentToast("New Usb device Attached", 1000);
            this.loadUsbDevices();
          } else if (response.data == 'USB_DEVICE_DETACHED') {
            this.toastSvc.presentToast("Usb device detached", 1000);
            this.loadUsbDevices();
          } else if (response.data == 'REGISTERED') {
            this.toastSvc.presentToast("Usb Attach/Detach listener registered", 1000);
          }
        }
      }
    })
    this.loadUsbDevices();
  }

  private async loadUsbDevices() {
    this.usbserialResponse = undefined;
    delete this.devices;
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

  async copyToClip() {
    await Clipboard.write({
      string: this.readData
    });
    this.toastSvc.presentToast("Copied to Clipboard", 1000);
  }

  async onDeviceSelected(item: Device) {
    const loading = await this.loadingController.create({
      message: 'Please wait...',
      duration: 3000
    });
    this.toastSvc.presentToast('device id:: '+item.device.deviceId, 1000);
    await loading.present();
    const usbSerialOptions: UsbSerialOptions = { deviceId: item.device.deviceId, portNum: item.port, baudRate: 38400, dataBits: 8 }
    this.usbserialResponse = await UsbSerial.openSerial(usbSerialOptions);
    console.log(this.usbserialResponse);
    this.toastSvc.presentToast("device response" + this.usbserialResponse.success);
    if (this.usbserialResponse.success) {
      this.toastSvc.presentToast("device response" + this.usbserialResponse.data);
      UsbSerial.registerReadCall((response: UsbSerialResponse) => {
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
    } else {
      this.toastSvc.presentToast("device response" + this.usbserialResponse.error);
    }
  }

  async sendCmnds() {
    if (this.sendCmnd.length > 0) {
      const result = await UsbSerial.writeSerial({data: this.sendCmnd});
      if (result.success && result.data) {
        this.toastSvc.presentToast("Write Serial Success: "+ result.data, 1000);
      } else {
        this.toastSvc.presentToast("Write Serial Fail: "+ result.error.message, 1000);
      }
    } else {
      this.toastSvc.presentToast("Can't send empty string to device", 1000);
    }
  }

  async closeSerial() {
    await UsbSerial.closeSerial();
    this.loadUsbDevices();
  }

}
