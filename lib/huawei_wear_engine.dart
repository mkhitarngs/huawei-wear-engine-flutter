import 'package:huawei_wear_engine_flutter/AuthCallback.dart';
import 'package:huawei_wear_engine_flutter/Device.dart';
import 'package:huawei_wear_engine_flutter/Permission.dart';
import 'package:huawei_wear_engine_flutter/PingCallback.dart';

import 'SendCallback.dart';
import 'huawei_wear_engine_platform_interface.dart';

class HuaweiWearEngine {
  Future<String?> getPlatformVersion() {
    return HuaweiWearEnginePlatform.instance.getPlatformVersion();
  }

  Future<bool?> hasAvailableDevices() {
    return HuaweiWearEnginePlatform.instance.hasAvailableDevices();
  }

  Future<bool?> checkPermission(Permission permission) {
    return HuaweiWearEnginePlatform.instance.checkPermission(permission);
  }

  Future<List<bool>?> checkPermissions(List<Permission> permissions) {
    return HuaweiWearEnginePlatform.instance.checkPermissions(
      permissions,
    );
  }

  Future<void> requestPermission(
    AuthCallBack authCallback,
    List<Permission> permissions,
  ) {
    return HuaweiWearEnginePlatform.instance.requestPermission(
      authCallback,
      permissions,
    );
  }

  Future<List<Device>?> getBondedDevices() {
    return HuaweiWearEnginePlatform.instance.getBondedDevices();
  }

  Future<bool?> isAppInstalled(Device device, String pkgName) async {
    return HuaweiWearEnginePlatform.instance.isAppInstalled(
      device,
      pkgName,
    );
  }

  Future<int?> getAppVersion(Device device, String pkgName) async {
    return HuaweiWearEnginePlatform.instance.getAppVersion(
      device,
      pkgName,
    );
  }

  Future<void> ping(
    Device device,
    String pkgName,
    PingCallback pingCallback,
  ) async {
    return HuaweiWearEnginePlatform.instance.ping(
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
    return HuaweiWearEnginePlatform.instance.send(
      connectedDevice,
      pkgName,
      fingerPrint,
      sendMessage,
      sendCallback
    );
  }
}
