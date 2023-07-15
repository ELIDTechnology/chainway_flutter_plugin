import 'package:flutter/services.dart';

import 'chainway_plugin_platform_interface.dart';

class ChainwayPlugin {

  Future<void> barcodeInitialize() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('barcode_init');
  }

  Future<void> startScan() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('scan');
  }
  Future<void> stopScan() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('barcode_stop_scan"');
  }

  Future<void> closeScan() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('barcode_close"');
  }

  Future<void> init_print() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('init_printer');
  }

  Future<void> print_speed() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('print_speed', {'speed': 5});
  }
  Future<void> print_test() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('print_qr_code');
  }

  Future<void> print_receipt() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('print_receipt');
  }

  static Stream<String> get barcodeStream {
    const  eventChannel = EventChannel('chainway_stream');

    return eventChannel.receiveBroadcastStream().cast<String>();
  }

  Future<void> nfcInit() async {
    const platform = MethodChannel('chainway');

    return await platform.invokeMethod('NFC_INT');
  }





   Future<void> nfcRead(

  {required Function(Map<dynamic, dynamic> tag) tag}) async {
    const platform = MethodChannel('chainway');
    Map<dynamic, dynamic> data = {};
    final pollingOptions = ['iso14443', 'iso15693', 'iso18092'];
    var a = await platform.invokeMethod('NFC_READ', {'pollingOptions': pollingOptions});
    print(a);
    return a;

    // platform.setMethodCallHandler((MethodCall call) async {
    //   print(call);
    //   switch (call.method) {
    //     case 'onDiscovered':
    //       Map<dynamic, dynamic> tagData = call.arguments;
    //       // Use tagData here
    //       data = tagData;
    //       break;
    //   // Handle other methods
    //   }
    // });
    // return data;
  }
}
