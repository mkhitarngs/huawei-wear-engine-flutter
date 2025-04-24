package com.example.huwei_wear_engine_flutter

import android.content.Context
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.device.DeviceClient


class WearEngineController(context: Context) {
    private var deviceClient: DeviceClient = HiWear.getDeviceClient(context)

    fun hasAvailableDevices(onResult: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        deviceClient.hasAvailableDevices()
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }
}