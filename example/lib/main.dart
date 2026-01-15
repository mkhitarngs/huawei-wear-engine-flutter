import 'dart:collection';
import 'dart:ffi';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:huawei_wear_engine_flutter/AuthCallback.dart';
import 'package:huawei_wear_engine_flutter/Device.dart';
import 'package:huawei_wear_engine_flutter/Message.dart';
import 'package:huawei_wear_engine_flutter/Permission.dart';
import 'package:huawei_wear_engine_flutter/PingCallback.dart';
import 'package:huawei_wear_engine_flutter/ReceiverCallback.dart';
import 'package:huawei_wear_engine_flutter/SendCallback.dart';
import 'package:huawei_wear_engine_flutter_example/components/PermissionUI.dart';
import 'package:huawei_wear_engine_flutter_example/utils/Pair.dart';
import 'package:huawei_wear_engine_flutter_example/utils/Utils.dart';
import 'package:huawei_wear_engine_flutter/huawei_wear_engine.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _huaweiWearEngineFlutterPlugin = HuaweiWearEngine();

  bool _hasAvailableDevices = false;
  final Map<Permission, Pair<bool, bool>> _permissions = {
    Permission.DEVICE_MANAGER: Pair(false, false),
    Permission.NOTIFY: Pair(false, false),
    Permission.SENSOR: Pair(false, false),
    Permission.MOTION_SENSOR: Pair(false, false),
    Permission.WEAR_USER_STATUS: Pair(false, false),
  };
  List<Device> _devices = [];
  Device? _selectedDevice = null;
  bool? _isAppInstalled = null;
  int? _appVersion = null;
  int? _pingResult = null;
  String? _lastReceivedMessage = null;
  bool _isReceiverRegistered = false;

  // UI status controls
  final TextEditingController _pkgNameController = TextEditingController();
  final TextEditingController _fingerPrintController = TextEditingController();
  final TextEditingController _msgController = TextEditingController();
  bool _enablePermissionBtns = false;

  @override
  void initState() {
    super.initState();
  }

  void _onHasAvailableDevicesClick() async {
    bool? hasAvailableDevices;
    try {
      hasAvailableDevices =
          await _huaweiWearEngineFlutterPlugin.hasAvailableDevices();
      setState(() {
        _hasAvailableDevices = hasAvailableDevices ?? false;
      });
    } on PlatformException {
      print("Oliver404 - HasAvailableDevices - ERROR");
    }
  }

  void _onSelectPermission(Permission permission, bool selected) async {
    setState(() {
      _permissions[permission]?.first = selected;
      _enablePermissionBtns = _permissions.values.any((value) => value.first);
    });
  }

  void _onCheckPermission(Permission permission) async {
    bool? granted;
    try {
      granted = await _huaweiWearEngineFlutterPlugin.checkPermission(permission);
      setState(() {
        _permissions[permission]?.second = granted ?? false;
      });
    } on PlatformException {
      print("Oliver404 - onCheckPermission - ERROR");
    }
  }

  void _onCheckPermissions() async {
    List<bool>? grantedLst;
    try {
      List<Permission> permissionLst =
          _permissions.entries
              .where((permission) => permission.value.first)
              .map((permission) => permission.key)
              .toList();
      grantedLst = await _huaweiWearEngineFlutterPlugin.checkPermissions(
        permissionLst,
      );
      setState(() {
        for (final (index, permission) in permissionLst.indexed) {
          _permissions[permission]?.second = grantedLst?[index] ?? false;
        }
      });
    } on PlatformException {
      print("Oliver404 - onCheckPermissions - ERROR");
    }
  }

  void _onRequestPermission(Permission permission) async {
    try {
      _AuthCallbackImpl authCallback = _AuthCallbackImpl(
        _onOkPermissions,
        _onCancelRequestPermissions,
      );
      await _huaweiWearEngineFlutterPlugin.requestPermission(authCallback, [
        permission,
      ]);
    } on PlatformException {
      printError("Oliver404 - onRequestPermission - ERROR");
    }
  }

  void _onRequestPermissions() async {
    try {
      _AuthCallbackImpl authCallback = _AuthCallbackImpl(
        _onOkPermissions,
        _onCancelRequestPermissions,
      );
      List<Permission> permissionLst =
          _permissions.entries
              .where((permission) => permission.value.first)
              .map((permission) => permission.key)
              .toList(); // Get only checked permissions
      await _huaweiWearEngineFlutterPlugin.requestPermission(
        authCallback,
        permissionLst,
      );
    } on PlatformException {
      print("Oliver404 - onRequestPermissions - ERROR");
    }
  }

  void _onCancelRequestPermissions() async {
    print("Oliver404 - onCancelRequestPermissions");
    _showToastMessage("Request permissions canceled!!!");
  }

  void _onOkPermissions(List<Permission> grantedPermissions) {
    printInfo("Oliver404 - onOkPermissions");
    // WidgetsBinding.instance.addPostFrameCallback((_) {
    //   if (mounted) {
    setState(() {
      for (final permission in grantedPermissions) {
        _permissions[permission]?.second = true;
      }
    });
    // }
    // });
  }

  void _onGetBondedDevices() async {
    List<Device>? devices;
    try {
      devices = await _huaweiWearEngineFlutterPlugin.getBondedDevices();
      setState(() {
        _devices = devices ?? [];
      });
    } on PlatformException {
      printError("Oliver404 - onGetBondedDevices - ERROR");
    }
  }

  void _onVerifyAppIsInstalled() async {
    if (_selectedDevice == null) {
      _showToastMessage("Select a device!!!");
    } else if (_pkgNameController.text.isEmpty) {
      _showToastMessage("Package name cannot be empty");
    } else {
      bool? result;
      try {
        result = await _huaweiWearEngineFlutterPlugin.isAppInstalled(
          _selectedDevice!,
          _pkgNameController.text,
        );
        setState(() {
          _isAppInstalled = result;
        });
      } on PlatformException {
        printError("Oliver404 - onVerifyAppIsInstalled - ERROR");
      }
    }
  }

  void _onGetAppVersion() async {
    if (_selectedDevice == null) {
      _showToastMessage("Select a device!!!");
    } else if (_pkgNameController.text.isEmpty) {
      _showToastMessage("Package name cannot be empty");
    } else {
      int? result;
      try {
        result = await _huaweiWearEngineFlutterPlugin.getAppVersion(
          _selectedDevice!,
          _pkgNameController.text,
        );
        setState(() {
          _appVersion = result;
        });
      } on PlatformException {
        printError("Oliver404 - onGetAppVersion - ERROR");
      }
    }
  }

  void _onPing() async {
    if (_selectedDevice == null) {
      _showToastMessage("Select a device!!!");
    } else if (_pkgNameController.text.isEmpty) {
      _showToastMessage("Package name cannot be empty");
    } else {
      try {
        _PingCallbackImpl pingCallback = _PingCallbackImpl((resultCode) {
          setState(() {
            _pingResult = resultCode;
          });
        });
        await _huaweiWearEngineFlutterPlugin.ping(
          _selectedDevice!,
          _pkgNameController.text,
          pingCallback,
        );
      } on PlatformException {
        printError("Oliver404 - onPing - ERROR");
      }
    }
  }

  void _onSendMessage() async {
    if (_selectedDevice == null) {
      _showToastMessage("Select a device!!!");
    } else if (_pkgNameController.text.isEmpty) {
      _showToastMessage("Package name cannot be empty");
    } else if (_fingerPrintController.text.isEmpty) {
      _showToastMessage("Finger print cannot be empty");
    } else if (_msgController.text.isEmpty) {
      _showToastMessage("Message cannot be empty");
    } else {
      try {
        _SendCallbackImpl sendCallback = _SendCallbackImpl(
          _onSendProgress,
          _onSendResult,
        );
        await _huaweiWearEngineFlutterPlugin.send(
          _selectedDevice!,
          _pkgNameController.text,
          _fingerPrintController.text,
          _msgController.text,
          sendCallback
        );
      } on PlatformException {
        printError("Oliver404 - onGetAppVersion - ERROR");
      }
    }
  }

  void _onToggleReceiver() async {
    if (_isReceiverRegistered) {
      try {
        await _huaweiWearEngineFlutterPlugin.unregisterReceiver();
        setState(() {
          _isReceiverRegistered = false;
        });
        _showToastMessage("Receiver unregistered");
      } on PlatformException {
        printError("Oliver404 - unregisterReceiver - ERROR");
      }
    } else {
      if (_selectedDevice == null) {
        _showToastMessage("Select a device!!!");
      } else if (_pkgNameController.text.isEmpty) {
        _showToastMessage("Package name cannot be empty");
      } else if (_fingerPrintController.text.isEmpty) {
        _showToastMessage("Finger print cannot be empty");
      } else {
        try {
          _ReceiverCallbackImpl receiverCallback = _ReceiverCallbackImpl((message) {
            setState(() {
              _lastReceivedMessage = String.fromCharCodes(message.payload);
            });
            _showToastMessage("Message received!");
          });
          await _huaweiWearEngineFlutterPlugin.registerReceiver(
            device: _selectedDevice!,
            pkgName: _pkgNameController.text,
            fingerPrint: _fingerPrintController.text,
            receiverCallback: receiverCallback,
          );
          setState(() {
            _isReceiverRegistered = true;
          });
          _showToastMessage("Receiver registered");
        } on PlatformException {
          printError("Oliver404 - registerReceiver - ERROR");
        }
      }
    }
  }

  void _onSendProgress(int progress) {
    _showToastMessage("Message send progress: $progress");
  }

  void _onSendResult(int codeResult) {
    _showToastMessage("Message result code: $codeResult");
  }

  void _showToastMessage(String message) {
    Fluttertoast.showToast(msg: message, toastLength: Toast.LENGTH_LONG);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                _sectionHasAvailableDevices(context),
                _sectionPermissions(context),
                _sectionDevices(context),
                _appSection(context),
                _messageSection(context),
                _receiverSection(context),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _emptyList() {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(children: [Icon(Icons.cancel), Text("Empty list")]),
    );
  }

  Widget _watchWidget(
    String name,
    bool connected,
    bool selected,
    Function() onTap,
  ) {
    return Card(
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              Text(name),
              Row(
                children: [
                  Text("Connected: "),
                  Icon(
                    Icons.circle,
                    color: connected ? Colors.green : Colors.red,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
      color: selected ? Colors.blueGrey : null,
    );
  }

  Widget _stepSection(String title, Widget body) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Text(title, style: Theme.of(context).textTheme.titleLarge),
        ),
        body,
      ],
    );
  }

  Widget _sectionHasAvailableDevices(BuildContext context) {
    return _stepSection(
      "STEP 1: Has available devices",
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Padding(
                padding: const EdgeInsets.only(left: 16.0),
                child: Text("Has available devices: "),
              ),
              SizedBox(
                width: 50,
                child: Icon(
                  Icons.watch,
                  color: _hasAvailableDevices ? Colors.green : Colors.red,
                ),
              ),
              SizedBox(
                width: 50,
                child: IconButton(
                  onPressed: _onHasAvailableDevicesClick,
                  icon: Icon(Icons.refresh),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _sectionPermissions(BuildContext context) {
    return _stepSection(
      "STEP 2: Verify and request permissions",
      Column(
        children: [
          ..._permissions.entries.map((permission) {
            return PermissionComponent(
              checked: permission.value.first,
              permission: permission.key,
              granted: permission.value.second,
              onPermissionCheked:
                  (checked) => _onSelectPermission(permission.key, checked),
              onVerifyPermission: () => _onCheckPermission(permission.key),
              onRequestPermission: () => _onRequestPermission(permission.key),
            );
          }).toList(),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton(
                onPressed: _enablePermissionBtns ? _onCheckPermissions : null,
                child: Row(children: [Text("Refresh"), Icon(Icons.refresh)]),
              ),
              ElevatedButton(
                onPressed: _enablePermissionBtns ? _onRequestPermissions : null,
                child: Row(
                  children: [Text("Request"), Icon(Icons.question_mark)],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _sectionDevices(BuildContext context) {
    return _stepSection(
      "STEP 3: Get and select device",
      Column(
        children: [
          ..._devices.map((device) {
            return _watchWidget(
              device.name,
              device.connected,
              _selectedDevice == device,
              () => {
                setState(() {
                  _selectedDevice = device;
                }),
              },
            );
          }).toList(),
          if (_devices.isEmpty) _emptyList(),
          ElevatedButton(
            onPressed: _onGetBondedDevices,
            child: Text("Get Devices"),
          ),
        ],
      ),
    );
  }

  Widget _appSection(BuildContext context) {
    return _stepSection(
      "STEP 4: Verify if app is installed and the version",
      Column(
        children: [
          TextField(
            controller: _pkgNameController,
            decoration: InputDecoration(
              labelText: 'Enter your package name',
              border: OutlineInputBorder(),
            ),
          ),
          Row(
            children: [
              Text("Is my app installed: "),
              if (_isAppInstalled != null)
                Icon(
                  Icons.circle,
                  color: _isAppInstalled! ? Colors.green : Colors.red,
                ),
              TextButton(
                onPressed: _onVerifyAppIsInstalled,
                child: Text("Check"),
              ),
            ],
          ),
          Row(
            children: [
              Text("App version: ${_appVersion ?? ""}"),
              TextButton(onPressed: _onGetAppVersion, child: Text("Check")),
            ],
          ),
          Row(
            children: [
              Text("Ping result: ${_pingResult ?? ""}"),
              TextButton(onPressed: _onPing, child: Text("Ping")),
            ],
          ),
        ],
      ),
    );
  }

  Widget _messageSection(BuildContext context) {
    return _stepSection(
      "STEP 5: Send text message",
      Column(
        children: [
          TextField(
            controller: _pkgNameController,
            decoration: InputDecoration(
              labelText: 'Enter your package name',
              border: OutlineInputBorder(),
            ),
          ),
          SizedBox(height: 16),
          TextField(
            controller: _fingerPrintController,
            decoration: InputDecoration(
              labelText: 'Enter your finger print',
              border: OutlineInputBorder(),
            ),
          ),
          SizedBox(height: 16),
          TextField(
            controller: _msgController,
            decoration: InputDecoration(
              labelText: 'Enter your text message',
              border: OutlineInputBorder(),
            ),
          ),
          SizedBox(height: 16),
          ElevatedButton(
            onPressed: _onSendMessage,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [Text("Send"), SizedBox(width: 8), Icon(Icons.send)],
            ),
          ),
        ],
      ),
    );
  }

  Widget _receiverSection(BuildContext context) {
    return _stepSection(
      "STEP 6: Receive message",
      Column(
        children: [
          Row(
            children: [
              ElevatedButton(
                onPressed: _onToggleReceiver,
                child: Text(_isReceiverRegistered ? "Unregister Receiver" : "Register Receiver"),
              ),
              SizedBox(width: 16),
              Icon(
                Icons.circle,
                color: _isReceiverRegistered ? Colors.green : Colors.red,
              ),
            ],
          ),
          SizedBox(height: 16),
          Text("Last received message:"),
          Container(
            padding: EdgeInsets.all(8),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Text(_lastReceivedMessage ?? "No message yet", style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

class _AuthCallbackImpl implements AuthCallBack {
  final void Function(List<Permission>) _onOk;
  final void Function() _onCancel;

  _AuthCallbackImpl(this._onOk, this._onCancel);

  @override
  void onCancel() {
    this._onCancel();
  }

  @override
  void onOk(List<Permission> grantedPermissions) {
    this._onOk(grantedPermissions);
  }
}

class _PingCallbackImpl implements PingCallback {
  final void Function(int) _onPingResult;

  _PingCallbackImpl(this._onPingResult);

  @override
  void onPingResult(int resultCode) {
    _onPingResult(resultCode);
  }
}

class _SendCallbackImpl implements SendCallback {
  final void Function(int) _onSendProgress;
  final void Function(int) _onSendResult;

  _SendCallbackImpl(this._onSendProgress, this._onSendResult);

  @override
  void onSendProgress(int progress) {
    _onSendProgress(progress);
  }

  @override
  void onSendResult(int resultCode) {
    _onSendResult(resultCode);
  }
}

class _ReceiverCallbackImpl implements ReceiverCallback {
  final void Function(Message) _onReceive;

  _ReceiverCallbackImpl(this._onReceive);

  @override
  void onReceive(Message message) {
    _onReceive(message);
  }
}
