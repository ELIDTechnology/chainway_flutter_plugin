import 'package:flutter/services.dart';

class ChainwayPlugin {
  final platform = const MethodChannel('chainway');
  Future<void> barcodeInitialize() async {
    return await platform.invokeMethod('barcode_init');
  }

  Future<void> startScan() async {
    return await platform.invokeMethod('scan');
  }

  Future<void> stopScan() async {
    return await platform.invokeMethod('barcode_stop_scan"');
  }

  Future<void> closeScan() async {
    return await platform.invokeMethod('barcode_close"');
  }

  Future<void> init_print() async {
    return await platform.invokeMethod('init_printer');
  }

  Future<void> print_speed() async {
    return await platform.invokeMethod('print_speed', {'speed': 5});
  }

  Future<void> print_test() async {
    return await platform.invokeMethod('print_qr_code');
  }

  Future<void> print_receipt() async {
    return await platform.invokeMethod('print_receipt');
  }

  static Stream<String> get barcodeStream {
    const eventChannel = EventChannel('chainway_stream');
    return eventChannel.receiveBroadcastStream().cast<String>();
  }
}
