package com.viewtrak.plugins.usbserial;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.viewtrak.plugins.usbserial.Utils.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.Error;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class UsbSerial implements SerialInputOutputManager.Listener {
    // call that will be used to send back data to the capacitor app
    private PluginCall readCall;
    // activity reference from UsbSerialPlugin
    private Activity mActivity;
    // call that will have data to open connection
    private PluginCall openSerialCall;

    // usb permission tag name
    public static final String USB_PERMISSION ="com.viewtrak.plugins.usbserial.USB_PERMISSION";

    private enum UsbPermission { Unknown, Requested, Granted, Denied }
    // logging tag
    private final String TAG = UsbSerial.class.getSimpleName();

    private boolean sleepOnPause;
    // I/O manager to handle new incoming serial data
    private SerialInputOutputManager usbIoManager;
    // Default Usb permission state
    private UsbPermission usbPermission = UsbPermission.Unknown;
    // The serial port that will be used in this plugin
    private UsbSerialPort usbSerialPort;
    // Usb serial port connection status
    private boolean connected = false;
    // USB permission broadcastreceiver
    private final BroadcastReceiver broadcastReceiver;
    private final Handler mainLooper;
    public UsbSerial() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(USB_PERMISSION.equals(intent.getAction())) {
                    usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                            ? UsbPermission.Granted : UsbPermission.Denied;
                    if (mActivity != null && openSerialCall != null) {
                        openSerial(mActivity, openSerialCall);
                    }
                }
            }
        };
        mainLooper = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onNewData(byte[] data) {
        mainLooper.post(() -> {
            updateReceivedData(data);
        });
    }

    @Override
    public void onRunError(Exception e) {
        mainLooper.post(() -> {
            updateReadDataError(e);
            disconnect();
        });
    }

    public JSObject openSerial(Activity activity, PluginCall openSerialCall) {
        JSObject obj = new JSObject();
        this.mActivity = activity;
        this.openSerialCall = openSerialCall;
        try {
            int deviceId = openSerialCall.hasOption("deviceId") ? openSerialCall.getInt("deviceId") : 0;
            int portNum = openSerialCall.hasOption("portNum") ? openSerialCall.getInt("portNum") : 0;
            int baudRate = openSerialCall.hasOption("baudRate") ? openSerialCall.getInt("baudRate") : 9600;
            int dataBits = openSerialCall.hasOption("dataBits") ?  openSerialCall.getInt("dataBits") : UsbSerialPort.DATABITS_8;
            int stopBits = openSerialCall.hasOption("stopBits") ?  openSerialCall.getInt("stopBits") : UsbSerialPort.STOPBITS_1;
            int parity = openSerialCall.hasOption("parity") ?  openSerialCall.getInt("parity") : UsbSerialPort.PARITY_NONE;
            boolean setDTR = openSerialCall.hasOption("dtr") && openSerialCall.getBoolean("dtr");
            boolean setRTS = openSerialCall.hasOption("rts") && openSerialCall.getBoolean("rts");
            // Sleep On Pause defaults to true
            this.sleepOnPause =  openSerialCall.hasOption("sleepOnPause") ? openSerialCall.getBoolean("sleepOnPause") : true;

            UsbDevice device = null;
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            for(UsbDevice v : usbManager.getDeviceList().values())
                if(v.getDeviceId() == deviceId)
                    device = v;
            if (device == null) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: device not found", new Throwable("connectionFailed:DeviceNotFound")));
                return obj;
            }
            UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
            if (driver == null) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: no driver for device", new Throwable("connectionFailed:NoDriverForDevice")));
                return obj;
            }
            if(driver.getPorts().size() < portNum) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: not enough ports at device", new Throwable("connectionFailed:NoAvailablePorts")));
                return obj;
            }
            usbSerialPort = driver.getPorts().get(portNum);
            UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
            if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
                usbPermission = UsbPermission.Requested;
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(USB_PERMISSION), 0);
                usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            }
            if(usbConnection == null) {
                if (!usbManager.hasPermission(driver.getDevice())) {
                    obj.put("success", false);
                    obj.put("error", new Error("connection failed: permission denied", new Throwable("connectionFailed:UsbConnectionPermissionDenied")));
                } else {
                    obj.put("success", false);
                    obj.put("error", new Error("connection failed: Serial open failed", new Throwable("connectionFailed:SerialOpenFailed")));
                }
                return obj;
            }
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, dataBits, stopBits, parity);
            if (setDTR) usbSerialPort.setDTR(true);
            if (setRTS) usbSerialPort.setRTS(true);
            obj.put("success", true);
            obj.put("data", "connection succeeded: Connection open");
            usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            usbIoManager.start();
            connected = true;
        } catch (Exception exception) {
            obj.put("success", false);
            obj.put("error", new Error(exception.getMessage(), exception.getCause()));
            disconnect();
        }
        return obj;
    }

    void onResume(Activity activity) {
        this.mActivity = activity;
        this.mActivity.registerReceiver(broadcastReceiver, new IntentFilter(USB_PERMISSION));
        if (sleepOnPause) {
            if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted)
                mainLooper.post(() -> {
                    openSerial(this.mActivity, this.openSerialCall);
                });
        }
    }

    void onPause(Activity activity) {
        this.mActivity = activity;
        if(connected && sleepOnPause) {
            disconnect();
        }
        this.mActivity.unregisterReceiver(broadcastReceiver);
    }

    private void disconnect() {
        connected = false;
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;
    }

    public JSObject readCall(PluginCall call) {
        JSObject jsObject = new JSObject();
        this.readCall = call;
        call.setKeepAlive(true);
        jsObject.put("success", true);
        jsObject.put("data", "registered".getBytes(Charset.defaultCharset()));
        return jsObject;
    }

    private void updateReceivedData(byte[] data) {
        if (this.readCall != null) {
            JSObject jsObject = new JSObject();
            this.readCall.setKeepAlive(true);
            try {
                String str = HexDump.toHexString(data);
                str.concat("\n");
                jsObject.put("data", str);
                jsObject.put("success", true);
            } catch (Exception exception) {
                jsObject.put("error", new Error(exception.getMessage(), exception.getCause()));
                jsObject.put("success", false);
            }
            readCall.resolve(jsObject);
        }
    }

    private void updateReadDataError(Exception exception) {
        if (readCall != null) {
            JSObject jsObject = new JSObject();
            jsObject.put("error", new Error(exception.getMessage(), exception.getCause()));
            jsObject.put("success", false);
            readCall.resolve(jsObject);
        }
    }

    public JSObject devices(Activity activity) {
        JSObject jsObject = new JSObject();
        try {
            List<DeviceItem> listItems = new ArrayList();
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
            for(UsbDevice device : usbManager.getDeviceList().values()) {
                UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
                if(driver != null) {
                    for(int port = 0; port < driver.getPorts().size(); port++)
                        listItems.add(new DeviceItem(device, port, driver));
                } else {
                    listItems.add(new DeviceItem(device, 0, null));
                }
            }
            JSONArray jsonArray = Utils.deviceListToJsonConvert(listItems);
            JSONObject data = new JSONObject();
            data.put("devices", jsonArray);
            jsObject.put("data", data);
            jsObject.put("success", true);
        } catch (Exception exception) {
            jsObject.put("error", new Error(exception.getMessage(), exception.getCause()));
        }
        return jsObject;
    }
}
