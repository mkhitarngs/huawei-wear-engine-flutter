package com.mkhitarngs.flutter.plugin.huawei_wear_engine

import android.content.Context
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.AuthClient
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.device.DeviceClient
import com.huawei.wearengine.p2p.Message
import com.huawei.wearengine.p2p.P2pClient
import com.huawei.wearengine.p2p.PingCallback
import com.huawei.wearengine.p2p.Receiver
import com.huawei.wearengine.p2p.SendCallback
import org.json.JSONObject
import java.io.File


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
        onFailure: (Exception) -> Unit,
    ) {
        authClient.checkPermissions(permissions)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun requestPermission(
        vararg permission: Permission,
        authCallback: AuthCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        authClient.requestPermission(authCallback, *permission)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun getBondedDevices(
        onSuccess: (List<Device>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        deviceClient.bondedDevices
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
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

    fun registerReceiver(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        receiver: Receiver,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.setPeerPkgName(pkgName)
        p2pClient.setPeerFingerPrint(fingerPrint)
        p2pClient.registerReceiver(connectedDevice, receiver)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun unregisterReceiver(
        receiver: Receiver,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.unregisterReceiver(receiver)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun isAppInstalled(
        connectedDevice: Device,
        pkgName: String,
        onResult: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.isAppInstalled(connectedDevice, pkgName)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun getAppVersion(
        connectedDevice: Device,
        pkgName: String,
        onResult: (Int) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.getAppVersion(connectedDevice, pkgName)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onFailure)
    }

    fun ping(
        connectedDevice: Device,
        pkgName: String,
        pingCallback: PingCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.setPeerPkgName(pkgName)
        p2pClient.ping(connectedDevice, pingCallback)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun send(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        sendMessage: String,
        sendCallback: SendCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        send(
            connectedDevice,
            pkgName,
            fingerPrint,
            buildMessage(sendMessage),
            sendCallback,
            onSuccess,
            onFailure
        )
    }

    fun send(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        sendMessage: Message,
        sendCallback: SendCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        p2pClient.setPeerPkgName(pkgName)
        p2pClient.setPeerFingerPrint(fingerPrint)
        p2pClient.send(connectedDevice, sendMessage, sendCallback)
            .addOnSuccessListener{ onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun buildMessage(message: String): Message {
        val builder = Message.Builder()
        builder.setPayload(message.encodeToByteArray())
        return builder.build()
    }

    fun buildMessageFromFile(filePath: String): Message {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: $filePath")
        }
        val builder = Message.Builder()
        builder.setPayload(file.readBytes())
        return builder.build()
    }

    fun buildMessageFromJson(jsonData: Map<String, Any>): Message {
        val jsonObject = JSONObject(jsonData)
        val jsonString = jsonObject.toString()
        val builder = Message.Builder()
        builder.setPayload(jsonString.encodeToByteArray())
        return builder.build()
    }

    fun buildMessageFromBytes(bytes: ByteArray): Message {
        val builder = Message.Builder()
        builder.setPayload(bytes)
        return builder.build()
    }

    fun sendFile(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        filePath: String,
        sendCallback: SendCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        try {
            val message = buildMessageFromFile(filePath)
            send(
                connectedDevice,
                pkgName,
                fingerPrint,
                message,
                sendCallback,
                onSuccess,
                onFailure
            )
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun sendJson(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        jsonData: Map<String, Any>,
        sendCallback: SendCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        try {
            val message = buildMessageFromJson(jsonData)
            send(
                connectedDevice,
                pkgName,
                fingerPrint,
                message,
                sendCallback,
                onSuccess,
                onFailure
            )
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun sendBytes(
        connectedDevice: Device,
        pkgName: String,
        fingerPrint: String,
        bytes: ByteArray,
        sendCallback: SendCallback,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        try {
            val message = buildMessageFromBytes(bytes)
            send(
                connectedDevice,
                pkgName,
                fingerPrint,
                message,
                sendCallback,
                onSuccess,
                onFailure
            )
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}