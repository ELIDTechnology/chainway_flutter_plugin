import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:chainway_plugin/chainway_plugin_method_channel.dart';

void main() {
  MethodChannelChainwayPlugin platform = MethodChannelChainwayPlugin();
  const MethodChannel channel = MethodChannel('chainway_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
