package com.viewtrak.plugins.usbserial;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsbSerial implements SerialInputOutputManager.Listener {
    // call that will be used to send back data to the capacitor app
    private PluginCall readCall;

    private enum UsbPermission { Unknown, Requested, Granted, Denied }

    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;
    private SerialInputOutputManager usbIoManager;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private UsbSerialPort usbSerialPort;
    private boolean connected = false;
//    private final BroadcastReceiver broadcastReceiver;
    private final Handler mainLooper;

    public UsbSerial() {
        /*broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(UsbBroadcastReceiver.USB_PERMISSION.equals(intent.getAction())) {
                    usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                            ? UsbPermission.Granted : UsbPermission.Denied;
//                    open();
                }
            }
        };*/
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

    public String echo(String value) {
        return value;
    }

    public JSObject open(Activity activity, int deviceId, int portNum, int baudRate, int dataBits) {
        JSObject obj = new JSObject();
        obj.put("success", false);
        obj.put("error", new Error("connection failed: connection not started", new Throwable("connectionFailed:ConnectionNotStarted")));
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if (device == null) {
            obj.put("success", false);
            obj.put("error", new Error("connection failed: device not found", new Throwable("connectionFailed:DeviceNotFound")));
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
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(UsbBroadcastReceiver.USB_PERMISSION), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            obj.put("success", false);
            obj.put("error", new Error("connection failed: Usb connection requested", new Throwable("connectionFailed:UsbConnectionRequested")));
            return obj;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
            {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: permission denied", new Throwable("connectionFailed:UsbConnectionPermissionDenied")));
            }
            else
            {
                obj.put("success", false);
                obj.put("error", new Error("connection failed: Connection open", new Throwable("connectionFailed:UsbConnectionOpen")));
            }
            return obj;
        }


        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, dataBits, 1, UsbSerialPort.PARITY_NONE);
            obj.put("success", true);
            obj.put("data", "connection succeeded: Connection open");
//            if(withIoManager) {
                usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
                usbIoManager.start();
//            }
//            status("connected");
            connected = true;
//            controlLines.start();
        } catch (Exception e) {
            obj.put("success", true);
            obj.put("error", new Error("connection failed: Connection open", new Throwable(e.getMessage())));
            disconnect();
        }
        return obj;
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
        jsObject.put("data", "USB serial read call registered");
        return jsObject;
    }

    private JSObject read() {
        JSObject jsObject = new JSObject();
        if(!connected) {
            jsObject.put("success", false);
            jsObject.put("error", new Error("Not Connected", new Throwable("NotConnected")));
            return jsObject;
        }
        try {
            byte[] buffer = new byte[8192];
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
            byte[] data = Arrays.copyOf(buffer, len);
            jsObject.put("success", true);
            jsObject.put("data", data);
            return jsObject;
        } catch (IOException e) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            jsObject.put("success", false);
            jsObject.put("error", new Error("connection lost: " + e.getMessage(), new Throwable("ConnectionLost")));
            disconnect();
            return jsObject;
        }
    }

    private void updateReceivedData(byte[] data) {
        if (this.readCall != null) {
            JSObject jsObject = new JSObject();
            this.readCall.setKeepAlive(true);
            jsObject.put("data", data);
            jsObject.put("success", true);
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
