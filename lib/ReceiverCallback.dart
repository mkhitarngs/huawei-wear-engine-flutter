import 'Message.dart';

abstract class ReceiverCallback {
  void onReceive(Message message);
}
