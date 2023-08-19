import 'package:flutter/services.dart';

class ChainwayPlugin {
  final platform = const MethodChannel('chainway');
  Future<bool> barcodeInitialize() async {
    return await platform.invokeMethod('barcode_init');
  }

  Future<void> startScan() async {
    return await platform.invokeMethod('barcode_scan');
  }

  Future<void> stopScan() async {
    return await platform.invokeMethod('barcode_stop_scan"');
  }

  Future<void> closeScan() async {
    return await platform.invokeMethod('barcode_close"');
  }

  Future<bool> init_print() async {
    return await platform.invokeMethod('printer_init');
  }

  Future<void> print_speed() async {
    return await platform.invokeMethod('print_speed', {'speed': 5});
  }

  Future<void> print_test() async {
    return await platform.invokeMethod('print_qr_code',
        {'print_qr_code': "HELLO WEIUWIQEUWIOQUEIOWUEIOWUYEIORWUYQR UWYRYW"});
  }

  Future<String> print_bitmap(
      {required Uint8List header,
      required String body,
      required String qrCode,
      required bool isHeader,
      required String footer}) async {
    var request = {
      "header": header,
      "isHeader": isHeader,
      "body": body,
      "qr": qrCode,
      "footer": footer,
    };
    return await platform.invokeMethod('print_bitmap', request);
  }

  Future<String> print_tests({
    required Uint8List header,
  }) async {
    var request = {
      "test": header,
    };
    return await platform.invokeMethod('print_test', request);
  }

  Future<String> print_receipt(String receipt) async {
    var request = {
      "receipt_details": receipt,
    };
    return await platform.invokeMethod('print_receipt', request);
  }

  Stream<String> get barcodeStream {
    const eventChannel = EventChannel('chainway_stream');
    return eventChannel.receiveBroadcastStream().cast<String>();
  }

  Stream<String> get printerCallback {
    const eventChannel = EventChannel('chainway_printer');
    return eventChannel.receiveBroadcastStream().cast<String>();
  }
}
