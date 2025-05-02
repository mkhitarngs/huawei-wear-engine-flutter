import 'package:flutter_test/flutter_test.dart';
import 'package:huawei_wear_engine_flutter/AuthCallback.dart';
import 'package:huawei_wear_engine_flutter/Device.dart';
import 'package:huawei_wear_engine_flutter/Permission.dart';
import 'package:huawei_wear_engine_flutter/PingCallback.dart';
import 'package:huawei_wear_engine_flutter/SendCallback.dart';
import 'package:huawei_wear_engine_flutter/huawei_wear_engine.dart';
import 'package:huawei_wear_engine_flutter/huawei_wear_engine_method_channel.dart';
import 'package:huawei_wear_engine_flutter/huawei_wear_engine_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockHuaweiWearEnginePlatform
    with MockPlatformInterfaceMixin
    implements HuaweiWearEnginePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<bool?> checkPermission(Permission permission) {
    // TODO: implement checkPermission
    throw UnimplementedError();
  }

  @override
  Future<List<bool>?> checkPermissions(List<Permission> permissions) {
    // TODO: implement checkPermissions
    throw UnimplementedError();
  }

  @override
  Future<int?> getAppVersion(Device device, String pkgName) {
    // TODO: implement getAppVersion
    throw UnimplementedError();
  }

  @override
  Future<List<Device>?> getBondedDevices() {
    // TODO: implement getBondedDevices
    throw UnimplementedError();
  }

  @override
  Future<bool?> hasAvailableDevices() {
    // TODO: implement hasAvailableDevices
    throw UnimplementedError();
  }

  @override
  Future<bool?> isAppInstalled(Device device, String pkgName) {
    // TODO: implement isAppInstalled
    throw UnimplementedError();
  }

  @override
  Future<void> ping(Device device, String pkgName, PingCallback pingCallback) {
    // TODO: implement ping
    throw UnimplementedError();
  }

  @override
  Future<void> requestPermission(AuthCallBack authCallback, List<Permission> permissions) {
    // TODO: implement requestPermission
    throw UnimplementedError();
  }

  @override
  Future<void> send(Device connectedDevice, String pkgName, String fingerPrint, String sendMessage, SendCallback sendCallback) {
    // TODO: implement send
    throw UnimplementedError();
  }
}

void main() {
  final HuaweiWearEnginePlatform initialPlatform = HuaweiWearEnginePlatform.instance;

  test('$MethodChannelHuaweiWearEngine is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelHuaweiWearEngine>());
  });

  test('getPlatformVersion', () async {
    HuaweiWearEngine huaweiWearEngineFlutterPlugin = HuaweiWearEngine();
    MockHuaweiWearEnginePlatform fakePlatform = MockHuaweiWearEnginePlatform();
    HuaweiWearEnginePlatform.instance = fakePlatform;

    expect(await huaweiWearEngineFlutterPlugin.getPlatformVersion(), '42');
  });
}
