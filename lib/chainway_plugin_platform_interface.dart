import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'chainway_plugin_method_channel.dart';

abstract class ChainwayPluginPlatform extends PlatformInterface {
  /// Constructs a ChainwayPluginPlatform.
  ChainwayPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static ChainwayPluginPlatform _instance = MethodChannelChainwayPlugin();

  /// The default instance of [ChainwayPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelChainwayPlugin].
  static ChainwayPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ChainwayPluginPlatform] when
  /// they register themselves.
  static set instance(ChainwayPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
