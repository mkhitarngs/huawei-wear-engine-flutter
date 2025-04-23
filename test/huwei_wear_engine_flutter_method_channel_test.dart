import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:huwei_wear_engine_flutter/huwei_wear_engine_flutter_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelHuweiWearEngineFlutter platform = MethodChannelHuweiWearEngineFlutter();
  const MethodChannel channel = MethodChannel('huwei_wear_engine_flutter');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
