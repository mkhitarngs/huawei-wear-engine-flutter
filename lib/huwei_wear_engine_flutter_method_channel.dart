import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:huwei_wear_engine_flutter/AuthCallback.dart';

import 'Device.dart';
import 'Permission.dart';
import 'PingCallback.dart';
import 'SendCallback.dart';
import 'huwei_wear_engine_flutter_platform_interface.dart';

/// An implementation of [HuweiWearEngineFlutterPlatform] that uses method channels.
class MethodChannelHuweiWearEngineFlutter
    extends HuweiWearEngineFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('huwei_wear_engine_flutter');
  @visibleForTesting
  final eventChannel = const EventChannel(
    "com.example.huwei_wear_engine_flutter/wear_engine",
  );

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
    final params = {
      "permissions": permissions.map((permission) => permission.value).toList(),
    };
    final result = await methodChannel.invokeMethod<List<Object?>>(
      'checkPermissions',
      params,
    );

    return result?.map((e) => e == true).toList();
  }

  @override
  Future<void> requestPermission(
    AuthCallBack authCallback,
    List<Permission> permissions,
  ) async {
    final params = {
      "permissions": permissions.map((permission) => permission.value).toList(),
    };
    // Listen events from Android Library method
    eventChannel.receiveBroadcastStream().listen((event) {
      String type = event["type"];
      if (type == "onOk") {
        List<Object?> result = event["result"];
        List<Permission> permissions =
            result
                .map((element) => Permission.fromString(element.toString()))
                .whereType<Permission>()
                .nonNulls
                .toList();
        authCallback.onOk(permissions);
      } else if (type == "onCancel") {
        authCallback.onCancel();
      }
    });

    await methodChannel.invokeMethod<void>('requestPermission', params);
  }

  @override
  Future<List<Device>?> getBondedDevices() async {
    final result = await methodChannel.invokeMethod<List<dynamic>>(
      'getBondedDevices',
    );
    final devices =
        result
            ?.map((item) => Device.fromMap(Map<String, dynamic>.from(item)))
            .toList();

    return devices;
  }

  @override
  Future<bool?> isAppInstalled(Device device, String pkgName) async {
    final params = {"device": device.toMap(), "pkgName": pkgName};
    final result = await methodChannel.invokeMethod<bool>(
      'isAppInstalled',
      params,
    );

    return result;
  }

  @override
  Future<int?> getAppVersion(Device device, String pkgName) async {
    final params = {"device": device.toMap(), "pkgName": pkgName};
    final result = await methodChannel.invokeMethod<int>(
      'getAppVersion',
      params,
    );

    return result;
  }

  @override
  Future<void> ping(
    Device device,
    String pkgName,
    PingCallback pingCallback,
  ) async {
    final params = {"device": device.toMap(), "pkgName": pkgName};

    eventChannel.receiveBroadcastStream().listen((event) {
      String type = event["type"];
      if (type == "onPingResult") {
        int result = event["result"];
        pingCallback.onPingResult(result);
      }
    });

    await methodChannel.invokeMethod<void>('ping', params);
  }

  @override
  Future<void> send(
    Device connectedDevice,
    String pkgName,
    String fingerPrint,
    String sendMessage,
    SendCallback sendCallback,
  ) async {
    final params = {
      "device": connectedDevice.toMap(),
      "pkgName": pkgName,
      "fingerPrint": fingerPrint,
      "message": sendMessage,
    };

    eventChannel.receiveBroadcastStream().listen((event) {
      String type = event["type"];
      if (type == "onSendResult") {
        int result = event["result"];
        sendCallback.onSendResult(result);
      } else if (type == "onSendProgress") {
        int result = event["result"];
        sendCallback.onSendProgress(result);
      }
    });

    await methodChannel.invokeMethod<void>('send', params);
  }
}
