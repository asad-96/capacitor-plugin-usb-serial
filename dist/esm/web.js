import { WebPlugin } from '@capacitor/core';
export class UsbSerialWeb extends WebPlugin {
    async usbAttachedDetached(callback) {
        return 'Usb device attached call not implemented on web yet!' + callback.name;
    }
    async connectedDevices() {
        return {
            success: false,
            error: { message: 'Loading connected devices not implemented on web yet!', cause: '' }
        };
    }
    async openSerial(options) {
        return {
            success: false,
            error: { message: 'Usb device open not implemented on web yet!, BaudRate ' + options.baudRate, cause: '' }
        };
    }
    async closeSerial() {
        return {
            success: false,
            error: { message: 'Usb device close not implemented on web yet!', cause: '' }
        };
    }
    async readSerial() {
        return {
            success: false,
            error: { message: 'Usb serial read not implemented on web yet!', cause: '' }
        };
    }
    async writeSerial(data) {
        return {
            success: false,
            error: { message: 'Usb serial write not implemented on web yet!' + data, cause: '' }
        };
    }
    async registerReadCall(callback) {
        return 'Usb read call not implemented on web yet!' + callback.name;
    }
}
//# sourceMappingURL=web.js.map