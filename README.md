# usb-serial-plugin

This plugin can be used for reading data from other device over the usb channel

## Install

```bash
npm install usb-serial-plugin
npx cap sync
```

## API

<docgen-index>

* [`usbAttachedDetached(...)`](#usbattacheddetached)
* [`connectedDevices()`](#connecteddevices)
* [`openSerial(...)`](#openserial)
* [`closeSerial()`](#closeserial)
* [`readSerial()`](#readserial)
* [`writeSerial(...)`](#writeserial)
* [`registerReadCall(...)`](#registerreadcall)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### usbAttachedDetached(...)

```typescript
usbAttachedDetached(callback: MyPluginCallback) => any
```

| Param          | Type                                                                               |
| -------------- | ---------------------------------------------------------------------------------- |
| **`callback`** | <code>(data: <a href="#usbserialresponse">UsbSerialResponse</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### connectedDevices()

```typescript
connectedDevices() => any
```

**Returns:** <code>any</code>

--------------------


### openSerial(...)

```typescript
openSerial(options: UsbSerialOptions) => any
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#usbserialoptions">UsbSerialOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### closeSerial()

```typescript
closeSerial() => any
```

**Returns:** <code>any</code>

--------------------


### readSerial()

```typescript
readSerial() => any
```

**Returns:** <code>any</code>

--------------------


### writeSerial(...)

```typescript
writeSerial(data: UsbSerialWriteOptions) => any
```

| Param      | Type                                                                    |
| ---------- | ----------------------------------------------------------------------- |
| **`data`** | <code><a href="#usbserialwriteoptions">UsbSerialWriteOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### registerReadCall(...)

```typescript
registerReadCall(callback: MyPluginCallback) => any
```

| Param          | Type                                                                               |
| -------------- | ---------------------------------------------------------------------------------- |
| **`callback`** | <code>(data: <a href="#usbserialresponse">UsbSerialResponse</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### UsbSerialResponse

| Prop          | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`success`** | <code>boolean</code>                                      |
| **`error`**   | <code><a href="#usbserialerror">UsbSerialError</a></code> |
| **`data`**    | <code>any</code>                                          |


#### UsbSerialError

| Prop          | Type                |
| ------------- | ------------------- |
| **`message`** | <code>string</code> |
| **`cause`**   | <code>string</code> |


#### UsbSerialOptions

| Prop               | Type                 |
| ------------------ | -------------------- |
| **`deviceId`**     | <code>number</code>  |
| **`portNum`**      | <code>number</code>  |
| **`baudRate`**     | <code>number</code>  |
| **`dataBits`**     | <code>number</code>  |
| **`stopBits`**     | <code>number</code>  |
| **`parity`**       | <code>number</code>  |
| **`dtr`**          | <code>boolean</code> |
| **`rts`**          | <code>boolean</code> |
| **`sleepOnPause`** | <code>boolean</code> |


#### UsbSerialWriteOptions

| Prop       | Type                |
| ---------- | ------------------- |
| **`data`** | <code>string</code> |

</docgen-api>
