import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  constructor(private toastCtrl: ToastController) {}

  async presentToast(message: string, duration: number = 5000) {
    const toast = await this.toastCtrl.create({
      message,
      duration,
    });
    toast.present();
  }

  async presentToastWithOptions(
    message: string,
    duration: number=5000,
    postion: 'bottom' | 'middle' | 'top' = 'bottom',
    cssClass: string = '',
    showCloseBtn: boolean = true,
    closeBtnText: string = 'Close'
  ) {
    let buttons = [];
    if (showCloseBtn) {
      buttons.push({
        side: 'end',
        icon: 'close',
        role: 'cancel',
        text: closeBtnText,
      });
    }
    const toast = await this.toastCtrl.create({
      message: message,
      position: postion,
      cssClass,
      duration,
      buttons,
    });
    toast.present();
  }

  // async showAlertReactivateRequest() {
  //   return new Promise(async (resolve: any) => {
  //     const toast = await this.toastCtrl.create({
  //       header: 'Account Suspended',
  //       message: 'You have previously cancelled your Pure Matrimony Account. Do you wish to reactivate your account?',
  //       position: 'bottom',
  //       buttons: [
  //         {
  //           side: 'end',
  //           role: 'cancel',
  //           text: 'Cancel',
  //           handler: () => {
  //             resolve(false);
  //           }
  //         }, {
  //           text: 'Reactivate',
  //           handler: () => {
  //             resolve(true);
  //           }
  //         }
  //       ]
  //     });
  //     await toast.present();
  //   });
  // }
}
