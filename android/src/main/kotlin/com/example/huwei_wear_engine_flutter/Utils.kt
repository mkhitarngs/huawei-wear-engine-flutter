package com.example.huwei_wear_engine_flutter

import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device

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

fun Device.toMap(): Map<String, Any> {
    return mapOf(
        "name" to this.name,
        "uuid" to this.uuid,
        "model" to this.model,
        "productType" to this.productType,
        "connected" to this.isConnected,
        "reservedness" to this.reservedness,
    )
}

fun mapToDevice(map: Map<String, Any>): Device {
    val device = Device()

    device.name = map["name"].toString()
    device.uuid = map["uuid"].toString()
    device.model = map["model"].toString()
    device.productType = (map["productType"] as Int)
    device.setConnectState(if(map["connected"] == true) 2 else 0)
    device.reservedness = map["reservedness"].toString()

    return device
}