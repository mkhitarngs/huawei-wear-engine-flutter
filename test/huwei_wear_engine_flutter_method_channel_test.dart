import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:huawei_wear_engine_flutter/Device.dart';
import 'package:huawei_wear_engine_flutter/Message.dart';
import 'package:huawei_wear_engine_flutter/ReceiverCallback.dart';
import 'package:huawei_wear_engine_flutter/huawei_wear_engine_method_channel.dart';

class MockReceiverCallback extends ReceiverCallback {
  Message? receivedMessage;
  @override
  void onReceive(Message message) {
    receivedMessage = message;
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelHuaweiWearEngine platform = MethodChannelHuaweiWearEngine();
  const MethodChannel channel = MethodChannel('huawei_wear_engine');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        switch (methodCall.method) {
          case 'getPlatformVersion':
            return '42';
          case 'registerReceiver':
            return null;
          case 'unregisterReceiver':
            return null;
          default:
            return null;
        }
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });

  test('registerReceiver and unregisterReceiver', () async {
    final device = Device(
      name: 'Test Device',
      uuid: 'uuid',
      model: 'model',
      productType: 1,
      connected: true,
      reservedness: '',
    );
    final callback = MockReceiverCallback();

    await platform.registerReceiver(
      device: device,
      pkgName: 'test.pkg',
      fingerPrint: 'fingerprint',
      receiverCallback: callback,
    );

    // Note: To test the actual reception, we would need to mock the EventChannel's stream.
    // This test ensures the MethodChannel calls are correctly configured.

    await platform.unregisterReceiver();
  });
}
