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

  static Permission? fromString(String strPermission) {
    Permission? permission;

    switch(strPermission) {
      case "device_manager":
        permission = DEVICE_MANAGER;
        break;

      case "notify":
        permission = NOTIFY;
        break;

      case "sensor":
        permission = SENSOR;
        break;

      case "motion_sensor":
        permission = MOTION_SENSOR;
        break;

      case "wear_user_status":
        permission = WEAR_USER_STATUS;
        break;
    };

    return permission;
  }
}