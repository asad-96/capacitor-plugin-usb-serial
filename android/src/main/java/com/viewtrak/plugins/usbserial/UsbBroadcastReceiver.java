package com.viewtrak.plugins.usbserial;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginResult;

public class UsbBroadcastReceiver extends BroadcastReceiver {
    // logging tag
    private final String TAG = UsbBroadcastReceiver.class.getSimpleName();
    // usb permission tag name
    public static final String USB_PERMISSION ="com.viewtrak.plugins.usbserial.USB_PERMISSION";
    // capacitor plugin call to notify the success/error to the cordova app
    private PluginCall pluginCall;
    // capacitor activity to use it to unregister this broadcast receiver
    private Activity activity;

    /**
     * Custom broadcast receiver that will handle the capacitor pluginCall context
     * @param pluginCall
     * @param activity
     */
    public UsbBroadcastReceiver(PluginCall pluginCall, Activity activity) {
        this.pluginCall = pluginCall;
        this.activity = activity;
    }

    /**
     * Handle permission answer
     * @param context
     * @param intent
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        JSObject object = new JSObject();
        if (USB_PERMISSION.equals(action)) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                Log.d(TAG, "Permission to connect to the device was accepted!");
                object.put("success", true);
                object.put("message", "Permission to connect to the device was accepted!");
                pluginCall.resolve(object);
            }
            else {
                Log.d(TAG, "Permission to connect to the device was denied!");
                object.put("success", false);
                object.put("message", "Permission to connect to the device was denied!");
                pluginCall.resolve(object);
            }
            // unregister the broadcast receiver since it's no longer needed
            activity.unregisterReceiver(this);
        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            object.put("success", true);
            object.put("message", "Usb device detached");
//            PluginResult result = new PluginResult();
            pluginCall.resolve(object);
            //activity.unregisterReceiver(this);
        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            object.put("success", true);
            object.put("message", "Usb device attached");
//            PluginResult result = new PluginResult();
            pluginCall.resolve(object);
            //activity.unregisterReceiver(this);
        }
    }
}
