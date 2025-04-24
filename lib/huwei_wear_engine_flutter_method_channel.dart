import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'huwei_wear_engine_flutter_platform_interface.dart';

/// An implementation of [HuweiWearEngineFlutterPlatform] that uses method channels.
class MethodChannelHuweiWearEngineFlutter extends HuweiWearEngineFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('huwei_wear_engine_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool?> has() async {
    final result = await methodChannel.invokeMethod<bool>('has');
    return result;
  }
}
