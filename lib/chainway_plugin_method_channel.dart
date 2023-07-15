import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'chainway_plugin_platform_interface.dart';

/// An implementation of [ChainwayPluginPlatform] that uses method channels.
class MethodChannelChainwayPlugin extends ChainwayPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('chainway_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
