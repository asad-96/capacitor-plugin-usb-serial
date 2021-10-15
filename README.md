# usb-serial-plugin

This plugin can be used for reading data from other device over the usb channel

## Install

```bash
npm install usb-serial-plugin
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`openSerial(...)`](#openserial)
* [`connectedDevices()`](#connecteddevices)
* [`registerReadCall(...)`](#registerreadcall)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => any
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

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


### connectedDevices()

```typescript
connectedDevices() => any
```

**Returns:** <code>any</code>

--------------------


### registerReadCall(...)

```typescript
registerReadCall(callback: MyPluginCallback) => any
```

| Param          | Type                                                                                                     |
| -------------- | -------------------------------------------------------------------------------------------------------- |
| **`callback`** | <code>(message: <a href="#usbserialresponse">UsbSerialResponse</a> \| null, err?: any) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### UsbSerialOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`deviceId`** | <code>number</code> |
| **`portNum`**  | <code>number</code> |
| **`baudRate`** | <code>number</code> |
| **`dataBits`** | <code>number</code> |


#### UsbSerialResponse

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`error`**   | <code>object</code>  |
| **`data`**    | <code>any</code>     |

</docgen-api>
