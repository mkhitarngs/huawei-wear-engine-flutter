import 'dart:typed_data';

class Message {
  final Uint8List payload;

  Message({required this.payload});

  factory Message.fromMap(Map<String, dynamic> map) {
    return Message(
      payload: map['payload'] as Uint8List,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'payload': payload,
    };
  }
}
