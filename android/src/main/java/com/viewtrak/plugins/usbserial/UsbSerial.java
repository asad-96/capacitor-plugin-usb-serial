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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Arrays;
import java.util.List;

public class UsbSerial implements SerialInputOutputManager.Listener {
    // call that will be used to send back usb device attached/detached event
    private PluginCall usbAttachedDetachedCall;
    // call that will be used to send back data to the capacitor app
    private PluginCall readCall;
    // activity reference from UsbSerialPlugin
    private AppCompatActivity mActivity;
    // call that will have data to open connection
    private PluginCall openSerialCall;

    // usb permission tag name
    public static final String USB_PERMISSION ="com.viewtrak.plugins.usbserial.USB_PERMISSION";
    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;

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
                String action = intent.getAction();
                if(USB_PERMISSION.equals(action)) {
                    usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                            ? UsbPermission.Granted : UsbPermission.Denied;
                    if (mActivity != null && openSerialCall != null) {
                        openSerial(mActivity, openSerialCall);
                        mActivity.unregisterReceiver(this);
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    if (usbAttachedDetachedCall != null) {
                        JSObject jsObject = new JSObject();
                        usbAttachedDetachedCall.setKeepAlive(true);
                        jsObject.put("success", true);
                        jsObject.put("data", "NEW_USB_DEVICE_ATTACHED");
                        usbAttachedDetachedCall.resolve(jsObject);
                    }
                }  else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    if (usbAttachedDetachedCall != null) {
                        JSObject jsObject = new JSObject();
                        usbAttachedDetachedCall.setKeepAlive(true);
                        jsObject.put("success", true);
                        jsObject.put("data", "USB_DEVICE_DETACHED");
                        usbAttachedDetachedCall.resolve(jsObject);
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

    public void openSerial(AppCompatActivity activity, PluginCall openSerialCall) {
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
            for(UsbDevice v : usbManager.getDeviceList().values()) {
                if (v.getDeviceId() == deviceId)
                    device = v;
            }
            if (device == null) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: device not found", new Throwable("connectionFailed:DeviceNotFound")));
                this.openSerialCall.resolve(obj);
                return;
            }
            UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
            if (driver == null) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: no driver for device", new Throwable("connectionFailed:NoDriverForDevice")));
                this.openSerialCall.resolve(obj);
                return;
            }
            if(driver.getPorts().size() < portNum) {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: not enough ports at device", new Throwable("connectionFailed:NoAvailablePorts")));
                this.openSerialCall.resolve(obj);
                return;
            }
            usbSerialPort = driver.getPorts().get(portNum);
            UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
            if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
                usbPermission = UsbPermission.Requested;
                int flags;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12 (S+) e versões mais recentes
                    flags = PendingIntent.FLAG_IMMUTABLE;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Versões anteriores a Android 12
                    flags = PendingIntent.FLAG_MUTABLE;
                } else {
                    flags = 0;
                }
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this.mActivity, 0, new Intent(USB_PERMISSION), flags);
                this.mActivity.registerReceiver(broadcastReceiver, new IntentFilter(USB_PERMISSION));
                usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
                return;
            }
            if(usbConnection == null) {
                if (!usbManager.hasPermission(driver.getDevice())) {
                    obj.put("success", false);
                    obj.put("error", new Error("connection failed: permission denied", new Throwable("connectionFailed:UsbConnectionPermissionDenied")));
                } else {
                    obj.put("success", false);
                    obj.put("error", new Error("connection failed: Serial open failed", new Throwable("connectionFailed:SerialOpenFailed")));
                }
                this.openSerialCall.resolve(obj);
                return;
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
        this.openSerialCall.resolve(obj);
    }

    public JSObject closeSerial() {
        JSObject jsObject = new JSObject();
        if (readCall != null) {
            jsObject.put("success", false);
            readCall.resolve();
        }
        // Make sure we don't die if we try to close an non-existing port!
        disconnect();
        jsObject.put("success", true);
        jsObject.put("data", "Connection Closed");
        return jsObject;
    }

    JSObject readSerial() {
        JSObject jsObject = new JSObject();
        if(!connected) {
            jsObject.put("error", new Error("not connected", new Throwable("NOT_CONNECTED")));
            jsObject.put("success", false);
            return jsObject;
        }
        try {
            byte[] buffer = new byte[8192];
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
            String str = HexDump.toHexString(Arrays.copyOf(buffer, len));
            str.concat("\n");
            jsObject.put("data", str);
            jsObject.put("success", true);
        } catch (IOException e) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            jsObject.put("success", false);
            jsObject.put("error", new Error("connection lost: " + e.getMessage(), e.getCause()));
            disconnect();
        }
        return jsObject;
    }

    JSObject writeSerial(String str) {
        JSObject jsObject = new JSObject();
        if(!connected) {
            jsObject.put("error", new Error("not connected", new Throwable("NOT_CONNECTED")));
            jsObject.put("success", false);
            return jsObject;
        }
        if(str.length() == 0) {
            jsObject.put("error", new Error("can't send empty string to device", new Throwable("EMPTY_STRING")));
            jsObject.put("success", false);
            return jsObject;
        }
        try {
            byte[] data = (str + "\r\n").getBytes();
            usbSerialPort.write(data, WRITE_WAIT_MILLIS);
            jsObject.put("data", str);
            jsObject.put("success", true);
            return jsObject;
        } catch (Exception e) {
            jsObject.put("success", false);
            jsObject.put("error", new Error("connection lost: " + e.getMessage(), e.getCause()));
            disconnect();
            return jsObject;
        }
    }


    void onResume() {
        if (sleepOnPause) {
            if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted)
                mainLooper.post(() -> {
                    openSerial(this.mActivity, this.openSerialCall);
                });
        }
    }

    void onPause() {
        if(connected && sleepOnPause) {
            disconnect();
        }
    }

    private void disconnect() {
        connected = false;
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        usbPermission = UsbPermission.Unknown;
        try {
            if (usbSerialPort != null)
                usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;
    }

    public JSObject readCall(PluginCall call) {
        JSObject jsObject = new JSObject();
        this.readCall = call;
        call.setKeepAlive(true);
        jsObject.put("success", true);
        jsObject.put("data", "REGISTERED".getBytes(Charset.defaultCharset()));
        return jsObject;
    }

    public JSObject usbAttachedDetached(AppCompatActivity activity, PluginCall call) {
        this.mActivity = activity;
        JSObject jsObject = new JSObject();
        usbAttachedDetachedCall = call;
        call.setKeepAlive(true);
        this.mActivity.registerReceiver(broadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        this.mActivity.registerReceiver(broadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        jsObject.put("success", true);
        jsObject.put("data", "REGISTERED");
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

    public JSObject devices(AppCompatActivity activity) {
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
