
import 'package:huwei_wear_engine_flutter/Permission.dart';

import 'huwei_wear_engine_flutter_platform_interface.dart';

class HuweiWearEngineFlutter {
  Future<String?> getPlatformVersion() {
    return HuweiWearEngineFlutterPlatform.instance.getPlatformVersion();
  }

  Future<bool?> hasAvailableDevices() {
    return HuweiWearEngineFlutterPlatform.instance.hasAvailableDevices();
  }

  Future<bool?> checkPermission(Permission permission) {
    return HuweiWearEngineFlutterPlatform.instance.checkPermission(permission);
  }

  Future<List<bool>?> checkPermissions(List<Permission> permissions) {
    return HuweiWearEngineFlutterPlatform.instance.checkPermissions(permissions);
  }

  Future<List<bool>?> requestPermission(List<Permission> permissions) {
    return HuweiWearEngineFlutterPlatform.instance.requestPermission(permissions);
  }
}
