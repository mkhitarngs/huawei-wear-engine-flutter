import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'Permission.dart';
import 'huwei_wear_engine_flutter_platform_interface.dart';

/// An implementation of [HuweiWearEngineFlutterPlatform] that uses method channels.
class MethodChannelHuweiWearEngineFlutter
    extends HuweiWearEngineFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('huwei_wear_engine_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<bool?> hasAvailableDevices() async {
    final result = await methodChannel.invokeMethod<bool>(
      'hasAvailableDevices',
    );
    return result;
  }

  @override
  Future<bool?> checkPermission(Permission permission) async {
    final params = {"permission": permission.value};
    final result = await methodChannel.invokeMethod<bool>(
      'checkPermission',
      params,
    );
    return result;
  }

  @override
  Future<List<bool>?> checkPermissions(List<Permission> permissions) async {
    final params = {"permissions": permissions.map((permission) => permission.value).toList()};
    final result = await methodChannel.invokeMethod<List<bool>>(
      'checkPermissions',
      params,
    );
    return result;
  }

  @override
  Future<List<bool>?> requestPermission(List<Permission> permissions) async {
    final params = {"permissions": permissions.map((permission) => permission.value).toList()};
    final result = await methodChannel.invokeMethod<List<bool>>(
      'requestPermission',
      params,
    );
    return result;
  }
}
