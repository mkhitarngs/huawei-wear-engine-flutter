import 'package:flutter/material.dart';
import 'package:huwei_wear_engine_flutter/Permission.dart';

class PermissionComponent extends StatelessWidget {
  final bool checked;
  final Permission permission;
  final bool granted;
  final void Function(bool valor) onPermissionCheked;
  final VoidCallback onVerifyPermission;
  final VoidCallback onRequestPermission;

  const PermissionComponent({
    super.key,
    required this.checked,
    required this.permission,
    required this.granted,
    required this.onPermissionCheked,
    required this.onVerifyPermission,
    required this.onRequestPermission,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        SizedBox(
          width: 50,
          child: Checkbox(
            value: checked,
            onChanged: (value) => onPermissionCheked(value == true),
          ),
        ),
        Expanded(child: Text(permission.value)),
        if (granted == true)
          SizedBox(width: 50, child: Icon(Icons.check, color: Colors.green))
        else
          SizedBox(width: 50, child: Icon(Icons.close, color: Colors.red)),

        IconButton(onPressed: onVerifyPermission, icon: Icon(Icons.refresh)),
        SizedBox(
          width: 80,
          child: TextButton(
            onPressed: onRequestPermission,
            child: Text("Request"),
          ),
        ),
      ],
    );
  }
}
