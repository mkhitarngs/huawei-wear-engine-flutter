package com.example.huwei_wear_engine_flutter

import com.huawei.wearengine.auth.Permission

fun String.toPermission(): Permission? {
    return when(this) {
        Permission.DEVICE_MANAGER.name -> Permission.DEVICE_MANAGER
        Permission.NOTIFY.name -> Permission.NOTIFY
        Permission.SENSOR.name -> Permission.SENSOR
        Permission.MOTION_SENSOR.name -> Permission.MOTION_SENSOR
        Permission.WEAR_USER_STATUS.name -> Permission.WEAR_USER_STATUS
        else -> null
    }
}