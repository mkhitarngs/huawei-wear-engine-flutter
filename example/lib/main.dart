import 'dart:collection';
import 'dart:ffi';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:huwei_wear_engine_flutter/Permission.dart';
import 'package:huwei_wear_engine_flutter/huwei_wear_engine_flutter.dart';
import 'package:huwei_wear_engine_flutter_example/components/PermissionUI.dart';
import 'package:huwei_wear_engine_flutter_example/utils/Pair.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _huweiWearEngineFlutterPlugin = HuweiWearEngineFlutter();

  bool _hasAvailableDevices = false;
  Map<Permission, Pair<bool, bool>> _permissions = {
    Permission.DEVICE_MANAGER: Pair(false, false),
    Permission.NOTIFY: Pair(false, false),
    Permission.SENSOR: Pair(false, false),
    Permission.MOTION_SENSOR: Pair(false, false),
    Permission.WEAR_USER_STATUS: Pair(false, false),
  };

  // UI status controls
  bool _enablePermissionBtns = false;


  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;

    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      bool? result = await _huweiWearEngineFlutterPlugin.hasAvailableDevices();
      print("Oliver404 $result");
      if (result == true)
        platformVersion = "TRUE";
      else
        platformVersion = "FALSE";
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  void _onHasAvailableDevicesClick() async {
    bool? hasAvailableDevices;
    try {
      hasAvailableDevices =
      await _huweiWearEngineFlutterPlugin.hasAvailableDevices();
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
      granted = await _huweiWearEngineFlutterPlugin.checkPermission(permission);
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
      List<Permission> permissionLst = _permissions.entries.where((
          permission) => permission.value.first).map((permission) =>
      permission.key).toList();
      grantedLst = await _huweiWearEngineFlutterPlugin.checkPermissions(permissionLst);
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
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _stepSection(String title, Widget body) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Text(title, style: Theme
              .of(context)
              .textTheme
              .titleLarge),
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
                    child: Row(
                      children: [
                        Text("Refresh"),
                        Icon(Icons.refresh)
                      ],
                    )
                ),
                ElevatedButton(
                    onPressed:  _enablePermissionBtns ? _onCheckPermissions : null,
                    child: Row(
                      children: [
                        Text("Request"),
                        Icon(Icons.question_mark)
                      ],
                    )
                ),
              ],
            )

          ]

      ),
    );
  }
}
