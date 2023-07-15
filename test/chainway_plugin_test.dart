import 'package:flutter_test/flutter_test.dart';
import 'package:chainway_plugin/chainway_plugin.dart';
import 'package:chainway_plugin/chainway_plugin_platform_interface.dart';
import 'package:chainway_plugin/chainway_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockChainwayPluginPlatform
    with MockPlatformInterfaceMixin
    implements ChainwayPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final ChainwayPluginPlatform initialPlatform = ChainwayPluginPlatform.instance;

  test('$MethodChannelChainwayPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelChainwayPlugin>());
  });

  test('getPlatformVersion', () async {
    ChainwayPlugin chainwayPlugin = ChainwayPlugin();
    MockChainwayPluginPlatform fakePlatform = MockChainwayPluginPlatform();
    ChainwayPluginPlatform.instance = fakePlatform;

    // expect(await chainwayPlugin.getPlatformVersion(), '42');
  });
}
