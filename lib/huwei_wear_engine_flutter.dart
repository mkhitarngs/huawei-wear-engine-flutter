
import 'huwei_wear_engine_flutter_platform_interface.dart';

class HuweiWearEngineFlutter {
  Future<String?> getPlatformVersion() {
    return HuweiWearEngineFlutterPlatform.instance.getPlatformVersion();
  }
}
