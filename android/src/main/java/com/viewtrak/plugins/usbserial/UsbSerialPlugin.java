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

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void openSerial(PluginCall call) {
        int deviceId = call.getInt("deviceId");
        int portNum = call.getInt("portNum");
        int baudRate = call.getInt("baudRate");
        int dataBits = call.getInt("dataBits");

        JSObject ret = implementation.open(getActivity(), deviceId, portNum, baudRate, dataBits);
        call.resolve(ret);
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
