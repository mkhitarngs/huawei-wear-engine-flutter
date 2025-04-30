class Device {
  static const int DEVICE_CONNECTED = 2;

  final String name;
  final String uuid;
  final String model;
  final int productType;
  final bool connected;
  final String reservedness;

  Device({
    required this.name,
    required this.uuid,
    required this.model,
    required this.productType,
    required this.connected,
    required this.reservedness,
  });

  factory Device.fromMap(Map<String, dynamic> map) {
    return Device(
      name: map['name'] as String,
      uuid: map['uuid'] as String,
      model: map['model'] as String,
      productType: map['productType'] as int,
      connected: map['connected'] as bool,
      reservedness: map['reservedness'] as String,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'uuid': uuid,
      'model': model,
      'productType': productType,
      'connected': connected,
      'reservedness': reservedness,
    };
  }
}
