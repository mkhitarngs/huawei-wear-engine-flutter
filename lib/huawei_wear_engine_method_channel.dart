import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'AuthCallback.dart';
import 'Device.dart';
import 'Message.dart';
import 'Permission.dart';
import 'PingCallback.dart';
import 'ReceiverCallback.dart';
import 'SendCallback.dart';
import 'huawei_wear_engine_platform_interface.dart';

/// An implementation of [HuaweiWearEnginePlatform] that uses method channels.
class MethodChannelHuaweiWearEngine extends HuaweiWearEnginePlatform {
  @visibleForTesting
  final MethodChannel methodChannel = const MethodChannel('huawei_wear_engine');

  @visibleForTesting
  final EventChannel eventChannel = const EventChannel(
    'com.mkhitarngs.flutter.plugin.huawei_wear_engine/wear_engine',
  );

  // ------------------------------------------------------------
  // Single shared EventChannel subscription
  // ------------------------------------------------------------
  StreamSubscription<dynamic>? _eventSubscription;
  bool _eventListenerReady = false;

  // ------------------------------------------------------------
  // Routed callbacks
  // ------------------------------------------------------------
  ReceiverCallback? _receiverCallback;

  final Map<int, SendCallback> _sendCallbacks = {};
  int _nextOpId = 1;

  PingCallback? _currentPingCallback;
  AuthCallBack? _currentAuthCallback;

  // ------------------------------------------------------------
  // Event listener
  // ------------------------------------------------------------
  void _ensureEventListener() {
    if (_eventListenerReady) return;

    debugPrint('[Flutter] [EVENT] Initializing shared event listener');

    _eventSubscription = eventChannel.receiveBroadcastStream().listen(
          (dynamic event) {
        if (event == null || event is! Map) return;

        final Object? t = event['type'];
        final String type = (t is String) ? t : '';

        if (type.isEmpty) return;

        debugPrint('[Flutter] [EVENT] Received event type: $type');

        switch (type) {
          case 'onMessageReceived':
            _handleOnMessageReceived(event);
            break;

          case 'onSendProgress':
            _handleOnSendProgress(event);
            break;

          case 'onSendResult':
            _handleOnSendResult(event);
            break;

          case 'onPingResult':
            _handleOnPingResult(event);
            break;

          case 'onOk':
            _handleOnAuthOk(event);
            break;

          case 'onCancel':
            _handleOnAuthCancel();
            break;

          default:
            debugPrint('[Flutter] [EVENT] Unknown event type: $type');
        }
      },
      onError: (Object err) {
        debugPrint('[Flutter] [EVENT] Stream error: $err');
      },
    );

    _eventListenerReady = true;
    debugPrint('[Flutter] [EVENT] Shared event listener initialized');
  }

  // ------------------------------------------------------------
  // Event handlers
  // ------------------------------------------------------------
  void _handleOnMessageReceived(Map event) {
    if (_receiverCallback == null) {
      debugPrint('[Flutter] [RECEIVE] onMessageReceived but no receiver callback registered');
      return;
    }

    final Object? r = event['result'];
    if (r == null || r is! Map) {
      debugPrint('[Flutter] [RECEIVE] Invalid message result type: ${r.runtimeType}');
      return;
    }

    final Map<String, dynamic> result = Map<String, dynamic>.from(r);

    // payload may be Uint8List or List<int> (or other). Log safely.
    final payload = result['payload'];
    int payloadSize = 0;
    if (payload is Uint8List) payloadSize = payload.length;
    else if (payload is List) payloadSize = payload.length;

    debugPrint('[Flutter] [RECEIVE] Message received, payload size: $payloadSize bytes');

    try {
      final message = Message.fromMap(result);
      _receiverCallback!.onReceive(message);
      debugPrint('[Flutter] [RECEIVE] receiverCallback.onReceive completed');
    } catch (e) {
      debugPrint('[Flutter] [RECEIVE] Message.fromMap failed: $e');
    }
  }

  void _handleOnSendProgress(Map event) {
    final int? opId = _readOpId(event);
    final SendCallback? cb = (opId != null) ? _sendCallbacks[opId] : null;

    if (cb == null) {
      debugPrint('[Flutter] [SEND] onSendProgress but no callback for opId: $opId');
      return;
    }

    // Kotlin sends Long -> Dart receives num
    final num raw = (event['result'] as num?) ?? 0;
    final int progress = raw.toInt();

    debugPrint('[Flutter] [SEND] onSendProgress (opId: $opId): $progress');
    cb.onSendProgress(progress);
  }

  void _handleOnSendResult(Map event) {
    final int? opId = _readOpId(event);
    final SendCallback? cb = (opId != null) ? _sendCallbacks[opId] : null;

    if (cb == null) {
      debugPrint('[Flutter] [SEND] onSendResult but no callback for opId: $opId');
      return;
    }

    final num raw = (event['result'] as num?) ?? -1;
    final int code = raw.toInt();

    debugPrint('[Flutter] [SEND] onSendResult (opId: $opId): $code');
    cb.onSendResult(code);

    // final callback -> cleanup
    _sendCallbacks.remove(opId);
    debugPrint('[Flutter] [SEND] Removed callback for opId: $opId');
  }

  void _handleOnPingResult(Map event) {
    if (_currentPingCallback == null) {
      debugPrint('[Flutter] [PING] onPingResult but no ping callback set');
      return;
    }

    final num raw = (event['result'] as num?) ?? -1;
    final int res = raw.toInt();

    debugPrint('[Flutter] [PING] onPingResult: $res');
    _currentPingCallback!.onPingResult(res);

    // One-shot callback
    _currentPingCallback = null;
  }

  void _handleOnAuthOk(Map event) {
    if (_currentAuthCallback == null) {
      debugPrint('[Flutter] [AUTH] onOk but no auth callback set');
      return;
    }

    final Object? r = event['result'];
    final List<Object?> list = (r is List<Object?>) ? r : const [];

    final permissions = list
        .map((e) => Permission.fromString(e.toString()))
        .whereType<Permission>()
        .toList();

    debugPrint('[Flutter] [AUTH] onOk permissions: ${permissions.map((p) => p.value).toList()}');
    _currentAuthCallback!.onOk(permissions);

    // One-shot callback
    _currentAuthCallback = null;
  }

  void _handleOnAuthCancel() {
    if (_currentAuthCallback == null) {
      debugPrint('[Flutter] [AUTH] onCancel but no auth callback set');
      return;
    }

    debugPrint('[Flutter] [AUTH] onCancel');
    _currentAuthCallback!.onCancel();

    // One-shot callback
    _currentAuthCallback = null;
  }

  int? _readOpId(Map event) {
    final Object? v = event['opId'];
    if (v is int) return v;
    if (v is num) return v.toInt();
    return null;
  }

  // ------------------------------------------------------------
  // Public API (Platform Interface)
  // ------------------------------------------------------------
  @override
  Future<String?> getPlatformVersion() async {
    return methodChannel.invokeMethod<String>('getPlatformVersion');
  }

  @override
  Future<bool?> hasAvailableDevices() async {
    return methodChannel.invokeMethod<bool>('hasAvailableDevices');
  }

  @override
  Future<bool?> checkPermission(Permission permission) async {
    final params = {'permission': permission.value};
    return methodChannel.invokeMethod<bool>('checkPermission', params);
  }

  @override
  Future<List<bool>?> checkPermissions(List<Permission> permissions) async {
    final params = {'permissions': permissions.map((p) => p.value).toList()};
    final result = await methodChannel.invokeMethod<List<Object?>>('checkPermissions', params);
    return result?.map((e) => e == true).toList();
  }

  @override
  Future<void> requestPermission(AuthCallBack authCallback, List<Permission> permissions) async {
    final params = {'permissions': permissions.map((p) => p.value).toList()};

    _currentAuthCallback = authCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('requestPermission', params);
  }

  @override
  Future<List<Device>?> getBondedDevices() async {
    final result = await methodChannel.invokeMethod<List<dynamic>>('getBondedDevices');
    return result
        ?.map((item) => Device.fromMap(Map<String, dynamic>.from(item as Map)))
        .toList();
  }

  @override
  Future<bool?> isAppInstalled(Device device, String pkgName) async {
    final params = {'device': device.toMap(), 'pkgName': pkgName};
    return methodChannel.invokeMethod<bool>('isAppInstalled', params);
  }

  @override
  Future<int?> getAppVersion(Device device, String pkgName) async {
    final params = {'device': device.toMap(), 'pkgName': pkgName};
    return methodChannel.invokeMethod<int>('getAppVersion', params);
  }

  @override
  Future<void> ping(Device device, String pkgName, PingCallback pingCallback) async {
    final params = {'device': device.toMap(), 'pkgName': pkgName};

    _currentPingCallback = pingCallback;
    _ensureEventListener();

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
    final int opId = _nextOpId++;
    debugPrint('[Flutter] [SEND] Starting send message (opId: $opId)');

    final params = {
      'device': connectedDevice.toMap(),
      'pkgName': pkgName,
      'fingerPrint': fingerPrint,
      'message': sendMessage,
      'opId': opId,
    };

    _sendCallbacks[opId] = sendCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('send', params);
  }

  @override
  Future<void> sendFile(
      Device connectedDevice,
      String pkgName,
      String fingerPrint,
      String filePath,
      SendCallback sendCallback,
      ) async {
    final int opId = _nextOpId++;
    debugPrint('[Flutter] [SEND_FILE] Starting send file (opId: $opId)');

    final params = {
      'device': connectedDevice.toMap(),
      'pkgName': pkgName,
      'fingerPrint': fingerPrint,
      'filePath': filePath,
      'opId': opId,
    };

    _sendCallbacks[opId] = sendCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('sendFile', params);
  }

  @override
  Future<void> sendJson(
      Device connectedDevice,
      String pkgName,
      String fingerPrint,
      Map<String, dynamic> jsonData,
      SendCallback sendCallback,
      ) async {
    final int opId = _nextOpId++;
    debugPrint('[Flutter] [SEND_JSON] Starting send JSON (opId: $opId)');

    final params = {
      'device': connectedDevice.toMap(),
      'pkgName': pkgName,
      'fingerPrint': fingerPrint,
      'jsonData': jsonData,
      'opId': opId,
    };

    _sendCallbacks[opId] = sendCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('sendJson', params);
  }

  @override
  Future<void> sendBytes(
      Device connectedDevice,
      String pkgName,
      String fingerPrint,
      List<int> bytes,
      SendCallback sendCallback,
      ) async {
    final int opId = _nextOpId++;
    debugPrint('[Flutter] [SEND_BYTES] Starting send bytes (opId: $opId)');

    final params = {
      'device': connectedDevice.toMap(),
      'pkgName': pkgName,
      'fingerPrint': fingerPrint,
      'bytes': bytes,
      'opId': opId,
    };

    _sendCallbacks[opId] = sendCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('sendBytes', params);
  }

  @override
  Future<void> registerReceiver({
    required Device device,
    required String pkgName,
    required String fingerPrint,
    required ReceiverCallback receiverCallback,
  }) async {
    debugPrint('[Flutter] [RECEIVE] registerReceiver called');

    final params = {
      'device': device.toMap(),
      'pkgName': pkgName,
      'fingerPrint': fingerPrint,
    };

    _receiverCallback = receiverCallback;
    _ensureEventListener();

    await methodChannel.invokeMethod<void>('registerReceiver', params);
  }

  @override
  Future<void> unregisterReceiver() async {
    _receiverCallback = null;
    await methodChannel.invokeMethod<void>('unregisterReceiver');
  }

  // ------------------------------------------------------------
  // Cleanup (recommended)
  // ------------------------------------------------------------
  Future<void> dispose() async {
    debugPrint('[Flutter] [EVENT] Disposing method channel implementation');

    await _eventSubscription?.cancel();
    _eventSubscription = null;
    _eventListenerReady = false;

    _receiverCallback = null;
    _currentPingCallback = null;
    _currentAuthCallback = null;
    _sendCallbacks.clear();
  }
}
