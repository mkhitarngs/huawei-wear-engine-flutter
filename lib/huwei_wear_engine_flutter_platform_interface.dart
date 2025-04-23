import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'huwei_wear_engine_flutter_method_channel.dart';

abstract class HuweiWearEngineFlutterPlatform extends PlatformInterface {
  /// Constructs a HuweiWearEngineFlutterPlatform.
  HuweiWearEngineFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static HuweiWearEngineFlutterPlatform _instance = MethodChannelHuweiWearEngineFlutter();

  /// The default instance of [HuweiWearEngineFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelHuweiWearEngineFlutter].
  static HuweiWearEngineFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [HuweiWearEngineFlutterPlatform] when
  /// they register themselves.
  static set instance(HuweiWearEngineFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
