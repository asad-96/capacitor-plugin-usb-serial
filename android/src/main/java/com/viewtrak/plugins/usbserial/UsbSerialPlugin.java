package com.viewtrak.plugins.usbserial;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "UsbSerial")
public class UsbSerialPlugin extends Plugin {

    private UsbSerial implementation = new UsbSerial();

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void usbAttachedDetached(PluginCall call) {
        JSObject ret = implementation.usbAttachedDetached(getActivity(), call);
        call.resolve(ret);
    }

    @PluginMethod
    public void connectedDevices(PluginCall call) {
        JSObject ret = implementation.devices(getActivity());
        call.resolve(ret);
    }

    @PluginMethod
    public void openSerial(PluginCall call) {
        implementation.openSerial(getActivity(), call);
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
        implementation.onResume();
    }

    @Override
    protected void handleOnPause() {
        implementation.onPause();
        super.handleOnPause();
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void registerReadCall(PluginCall call) {
        JSObject ret = implementation.readCall(call);
        call.resolve(ret);
    }
}
