import 'dart:typed_data';

class Message {
  final Uint8List payload;

  Message({required this.payload});

  factory Message.fromMap(Map<String, dynamic> map) {
    final dynamic raw = map['payload'];

    if (raw == null) {
      return Message(payload: Uint8List(0));
    }

    // Most common: already a Uint8List
    if (raw is Uint8List) {
      return Message(payload: raw);
    }

    // Also common: List<int>
    if (raw is List) {
      return Message(payload: Uint8List.fromList(raw.cast<int>()));
    }

    // Typed list variants
    if (raw is ByteBuffer) {
      return Message(payload: raw.asUint8List());
    }

    if (raw is Int8List) {
      return Message(payload: Uint8List.view(raw.buffer, raw.offsetInBytes, raw.lengthInBytes));
    }

    if (raw is Int32List) {
      // fallback: convert each element to byte (0..255)
      final bytes = raw.map((e) => e & 0xFF).toList();
      return Message(payload: Uint8List.fromList(bytes));
    }

    throw ArgumentError('Unsupported payload type: ${raw.runtimeType}');
  }

  Map<String, dynamic> toMap() => {'payload': payload};
}
