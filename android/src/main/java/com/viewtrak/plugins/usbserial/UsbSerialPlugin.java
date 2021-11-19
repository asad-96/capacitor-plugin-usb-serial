package com.viewtrak.plugins.usbserial;

import android.Manifest;
import android.app.Activity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginResult;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(name = "UsbSerial")
public class UsbSerialPlugin extends Plugin {

    private UsbSerial implementation = new UsbSerial();

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void usbDeviceAttachedDetached(PluginCall call) {
        JSObject ret = implementation.usbAttachedDetached(call);
        call.resolve(ret);
    }

    @PluginMethod
    public void connectedDevices(PluginCall call) {
        JSObject ret = implementation.devices(getActivity());
        call.resolve(ret);
    }

    @PluginMethod
    public void openSerial(PluginCall call) {
        JSObject ret = implementation.openSerial(getActivity(), call);
        call.resolve(ret);
    }

    @PluginMethod
    public void closeSerial(PluginCall call) {
        JSObject ret = implementation.closeSerial();
        call.resolve(ret);
    }

    @PluginMethod
    public void readSerial(PluginCall call) {
        JSObject ret = implementation.readSerial();
        call.resolve(ret);
    }

    @PluginMethod
    public void writeSerial(PluginCall call) {
        String data = call.hasOption("data") ? call.getString("data") : "";
        JSObject ret = implementation.writeSerial(data);
        call.resolve(ret);
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();
        implementation.onResume(getActivity());
    }

    @Override
    protected void handleOnPause() {
        implementation.onPause(getActivity());
        super.handleOnPause();
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void registerReadCall(PluginCall call) {
        JSObject ret = implementation.readCall(call);
        call.resolve(ret);
    }
}
