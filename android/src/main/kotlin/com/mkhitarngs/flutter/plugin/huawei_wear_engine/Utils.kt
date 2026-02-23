package com.mkhitarngs.flutter.plugin.huawei_wear_engine

import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.Message

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

fun Message.toMap(): Map<String, Any> {
    return mapOf(
        "payload" to this.data,
    )
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

    device.name = map["name"]?.toString().orEmpty()
    device.uuid = map["uuid"]?.toString().orEmpty()
    device.model = map["model"]?.toString().orEmpty()

    val pt = map["productType"]
    device.productType = (pt as? Number)?.toInt() ?: 0

    device.setConnectState(if (map["connected"] == true) 2 else 0)

    device.reservedness = map["reservedness"]?.toString().orEmpty()
    return device
}