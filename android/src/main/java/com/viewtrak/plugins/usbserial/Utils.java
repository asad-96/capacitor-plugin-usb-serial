package com.viewtrak.plugins.usbserial;

import android.hardware.usb.UsbDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Utils {

    static class DeviceItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        DeviceItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    public static JSONArray deviceListToJsonConvert(List<DeviceItem> list) {

        JSONArray jsonArray = new JSONArray();// /ItemDetail jsonArray

        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();// /sub Object

            try {
                JSONObject device = new JSONObject();
                device.put("productId", list.get(i).device.getProductId());
                device.put("productName", list.get(i).device.getProductName());
                device.put("vendorId", list.get(i).device.getVendorId());
                device.put("deviceId", list.get(i).device.getDeviceId());
                jsonObject.put("device", device);
                jsonObject.put("port", list.get(i).port);
                jsonObject.put("driver", list.get(i).driver);

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return  jsonArray;
    }

}
