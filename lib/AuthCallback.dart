import 'package:huwei_wear_engine_flutter/Permission.dart';

abstract class AuthCallBack {
  void onOk(List<Permission> grantedPermissions);
  void onCancel();
}