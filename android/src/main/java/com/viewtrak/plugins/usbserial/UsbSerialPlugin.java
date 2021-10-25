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
/*
    @PluginMethod
    public void requestPermission(PluginCall call) {
        implementation.requestPermission(call);
    }*/

    @PluginMethod
    public void openSerial(PluginCall call) {
        JSObject ret = implementation.openSerial(getActivity(), call);
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

    @PluginMethod
    public void connectedDevices(PluginCall call) {
        JSObject ret = implementation.devices(getActivity());
        call.resolve(ret);
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void registerReadCall(PluginCall call) {
        JSObject ret = implementation.readCall(call);
        call.resolve(ret);
    }
}
