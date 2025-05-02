
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'AuthCallback.dart';
import 'Device.dart';
import 'Permission.dart';
import 'PingCallback.dart';
import 'SendCallback.dart';
import 'huawei_wear_engine_method_channel.dart';

abstract class HuaweiWearEnginePlatform extends PlatformInterface {
  /// Constructs a HuaweiWearEngineFlutterPlatform.
  HuaweiWearEnginePlatform() : super(token: _token);

  static final Object _token = Object();

  static HuaweiWearEnginePlatform _instance = MethodChannelHuaweiWearEngine();

  /// The default instance of [HuaweiWearEnginePlatform] to use.
  ///
  /// Defaults to [MethodChannelHuweiWearEngine].
  static HuaweiWearEnginePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [HuaweiWearEngineFlutterPlatform] when
  /// they register themselves.
  static set instance(HuaweiWearEnginePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> hasAvailableDevices() {
    throw UnimplementedError('hasAvailableDevices() has not been implemented.');
  }

  Future<bool?> checkPermission(Permission permission) {
    throw UnimplementedError('checkPermission() has not been implemented.');
  }

  Future<List<bool>?> checkPermissions(List<Permission> permissions) {
    throw UnimplementedError('checkPermissions() has not been implemented.');
  }

  Future<void> requestPermission(
    AuthCallBack authCallback,
    List<Permission> permissions,
  ) {
    throw UnimplementedError('requestPermission() has not been implemented.');
  }

  Future<List<Device>?> getBondedDevices() {
    throw UnimplementedError('getBondedDevices() has not been implemented.');
  }

  Future<bool?> isAppInstalled(Device device, String pkgName) {
    throw UnimplementedError('isAppInstalled() has not been implemented.');
  }

  Future<int?> getAppVersion(Device device, String pkgName) {
    throw UnimplementedError('getAppVersion() has not been implemented.');
  }

  Future<void> ping(Device device, String pkgName, PingCallback pingCallback) {
    throw UnimplementedError('ping() has not been implemented.');
  }

  Future<void> send(
    Device connectedDevice,
    String pkgName,
    String fingerPrint,
    String sendMessage,
    SendCallback sendCallback,
  ) {
    throw UnimplementedError('send() has not been implemented.');
  }
}
