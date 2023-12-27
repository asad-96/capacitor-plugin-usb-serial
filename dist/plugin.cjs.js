'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const UsbSerial = core.registerPlugin('UsbSerial', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.UsbSerialWeb()),
});

class UsbSerialWeb extends core.WebPlugin {
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

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    UsbSerialWeb: UsbSerialWeb
});

exports.UsbSerial = UsbSerial;
//# sourceMappingURL=plugin.cjs.js.map
