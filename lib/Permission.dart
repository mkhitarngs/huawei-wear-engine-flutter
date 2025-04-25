class Permission {
  static const Permission DEVICE_MANAGER = Permission._("device_manager");
  static const Permission NOTIFY = Permission._("notify");
  static const Permission SENSOR = Permission._("sensor");
  static const Permission MOTION_SENSOR = Permission._("motion_sensor");
  static const Permission WEAR_USER_STATUS = Permission._("wear_user_status");

  final String value;

  const Permission._(this.value);

  @override
  String toString() => value;
}