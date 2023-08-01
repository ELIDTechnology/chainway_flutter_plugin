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
    return await platform.invokeMethod('print_qr_code',
        {'print_qr_code': "HELLO WEIUWIQEUWIOQUEIOWUEIOWUYEIORWUYQR UWYRYW"});
  }

  Future<void> print_bitmap(
      {required String body,
      required String qrCode,
      required String footer}) async {
    final ByteData bytes = await rootBundle.load('assets/test.jpg');
    final Uint8List list = bytes.buffer.asUint8List();

    var request = {
      "header": list,
      "body": body,
      "qr": qrCode,
      "footer": footer,
    };
    return await platform.invokeMethod('print_bitmap', request);
  }

  Future<void> print_receipt() async {
    return await platform.invokeMethod('print_receipt');
  }

  static Stream<String> get barcodeStream {
    const eventChannel = EventChannel('chainway_stream');
    return eventChannel.receiveBroadcastStream().cast<String>();
  }

  static Stream<String> get printerCallback {
    const eventChannel = EventChannel('printer_callback');
    return eventChannel.receiveBroadcastStream().cast<String>();
  }
}
