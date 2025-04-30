import 'package:huwei_wear_engine_flutter/AuthCallback.dart';
import 'package:huwei_wear_engine_flutter/Device.dart';
import 'package:huwei_wear_engine_flutter/Permission.dart';
import 'package:huwei_wear_engine_flutter/PingCallback.dart';

import 'SendCallback.dart';
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
    return HuweiWearEngineFlutterPlatform.instance.checkPermissions(
      permissions,
    );
  }

  Future<void> requestPermission(
    AuthCallBack authCallback,
    List<Permission> permissions,
  ) {
    return HuweiWearEngineFlutterPlatform.instance.requestPermission(
      authCallback,
      permissions,
    );
  }

  Future<List<Device>?> getBondedDevices() {
    return HuweiWearEngineFlutterPlatform.instance.getBondedDevices();
  }

  Future<bool?> isAppInstalled(Device device, String pkgName) async {
    return HuweiWearEngineFlutterPlatform.instance.isAppInstalled(
      device,
      pkgName,
    );
  }

  Future<int?> getAppVersion(Device device, String pkgName) async {
    return HuweiWearEngineFlutterPlatform.instance.getAppVersion(
      device,
      pkgName,
    );
  }

  Future<void> ping(
    Device device,
    String pkgName,
    PingCallback pingCallback,
  ) async {
    return HuweiWearEngineFlutterPlatform.instance.ping(
      device,
      pkgName,
      pingCallback,
    );
  }

  Future<void> send(
      Device connectedDevice,
      String pkgName,
      String fingerPrint,
      String sendMessage,
      SendCallback sendCallback,
      ) async {
    return HuweiWearEngineFlutterPlatform.instance.send(
      connectedDevice,
      pkgName,
      fingerPrint,
      sendMessage,
      sendCallback
    );
  }
}
