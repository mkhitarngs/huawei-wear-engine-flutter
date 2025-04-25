package com.example.huwei_wear_engine_flutter

import android.content.Context
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.AuthClient
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.device.DeviceClient
import com.huawei.wearengine.p2p.Message
import com.huawei.wearengine.p2p.P2pClient
import com.huawei.wearengine.p2p.SendCallback


class WearEngineController(context: Context) {
    private var deviceClient: DeviceClient = HiWear.getDeviceClient(context)
    var authClient: AuthClient = HiWear.getAuthClient(context)
    var p2pClient: P2pClient = HiWear.getP2pClient(context)

    fun hasAvailableDevices(onResult: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        deviceClient.hasAvailableDevices()
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun checkPermission(
        permission: Permission,
        onResult: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        authClient.checkPermission(permission)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun checkPermissions(
        permissions: Array<Permission>,
        onResult: (Array<out Boolean>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authClient.checkPermissions(permissions)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun requestPermission(
        vararg permission: Permission,
        authCallback: AuthCallback,
        onSuccess: (Void) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authClient.requestPermission(authCallback, *permission)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun getBondedDevices() {
        deviceClient.bondedDevices
            .addOnSuccessListener { devices -> // The device list is obtained.
                // TODO: Add callback
            }
            .addOnFailureListener {
                // TODO: Add callback
                // Process logic when the device list fails to be obtained.
            }
    }

    fun getCommonDevice() {
        TODO("To implement")
    }

    fun queryDeviceCapability() {
        TODO("To implement")
    }

    fun getDeviceSn() {
        TODO("To implement")
    }

    fun getAvailableKbytes() {
        TODO("To implement")
    }

    fun query() {
        TODO("To implement")
    }

    fun register() {
        TODO("To implement")
    }

    fun unregister() {
        TODO("To implement")
    }

    fun isAppInstalled(connectedDevice: Device, pkgName: String) {
        p2pClient.isAppInstalled(connectedDevice, pkgName)
            .addOnSuccessListener {
                // TODO: Add callback
                // The related logic to check whether the installation task of the app on the specified device is successfully executed.
                // true: installed, false: uninstalled
            }
            .addOnFailureListener {
                // TODO: Add callback
                // The related logic to check whether the installation task of the app on the specified device fails to be executed.
            }
    }

    fun getAppVersion(connectedDevice: Device, pkgName: String) {
        p2pClient.getAppVersion(connectedDevice, pkgName)
            .addOnSuccessListener {
                // TODO: Add callback
                // The logic used when the task of obtaining the app version number on the device is successfully executed.
                // -1: The app has not been installed.
            }
            .addOnFailureListener {
                // TODO: Add callback
                // The logic used when the task of obtaining the app version number on the device fails to be executed.
            }
    }

    fun ping(connectedDevice: Device, pkgName: String) {
        p2pClient.setPeerPkgName(pkgName);
        p2pClient.ping(connectedDevice) {
            // Result of communicating with the peer device using the ping method.
            // If the errCode value is 200, your app has not been installed on the wearable device. If the errCode value is 201, your app has been installed but not started on the wearable device. If the errCode value is 202, your app has been started on the wearable device.
        }.addOnSuccessListener {
            // Related processing logic for your app after the ping method is successfully executed.
        }.addOnFailureListener {
            // Related processing logic for your app after the ping method fails.
        }
    }

    fun send(
        connectedDevice: Device,
        sendMessage: Message,
        sendCallback: SendCallback,
    ) {
//        val sendCallback = object : SendCallback {
//            override fun onSendResult(result: Int) {
//                onSendResult(result)
//            }
//            override fun onSendProgress(progress: Long) {
//                onSendProgress(progress)
//            }
//        }

        p2pClient.send(connectedDevice, sendMessage, sendCallback)
            .addOnSuccessListener {
                // TODO: Add callback
                // Related processing logic for your app after the send task is successfully executed.
            }
            .addOnFailureListener {
                // TODO: Add callback
                // Related processing logic for your app after the send task fails.
            }
    }
}