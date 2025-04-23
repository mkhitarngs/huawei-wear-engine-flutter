import 'package:flutter_test/flutter_test.dart';
import 'package:huwei_wear_engine_flutter/huwei_wear_engine_flutter.dart';
import 'package:huwei_wear_engine_flutter/huwei_wear_engine_flutter_platform_interface.dart';
import 'package:huwei_wear_engine_flutter/huwei_wear_engine_flutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockHuweiWearEngineFlutterPlatform
    with MockPlatformInterfaceMixin
    implements HuweiWearEngineFlutterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final HuweiWearEngineFlutterPlatform initialPlatform = HuweiWearEngineFlutterPlatform.instance;

  test('$MethodChannelHuweiWearEngineFlutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelHuweiWearEngineFlutter>());
  });

  test('getPlatformVersion', () async {
    HuweiWearEngineFlutter huweiWearEngineFlutterPlugin = HuweiWearEngineFlutter();
    MockHuweiWearEngineFlutterPlatform fakePlatform = MockHuweiWearEngineFlutterPlatform();
    HuweiWearEngineFlutterPlatform.instance = fakePlatform;

    expect(await huweiWearEngineFlutterPlugin.getPlatformVersion(), '42');
  });
}
