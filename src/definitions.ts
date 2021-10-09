export interface UsbSerialPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
