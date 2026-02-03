package com.mkhitarngs.flutter.plugin.huawei_wear_engine

import android.content.Context
import android.util.Log
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

private const val TAG = "WearEngineController"


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
        Log.d(TAG, "[Kotlin] [RECEIVE] registerReceiver called")
        Log.d(TAG, "[Kotlin] [RECEIVE] Device: ${connectedDevice.toMap()}, pkgName: $pkgName, fingerPrint: $fingerPrint")
        Log.d(TAG, "[Kotlin] [RECEIVE] Setting peer package name: $pkgName")
        p2pClient.setPeerPkgName(pkgName)
        Log.d(TAG, "[Kotlin] [RECEIVE] Setting peer fingerprint: $fingerPrint")
        p2pClient.setPeerFingerPrint(fingerPrint)
        Log.d(TAG, "[Kotlin] [RECEIVE] Calling p2pClient.registerReceiver")
        p2pClient.registerReceiver(connectedDevice, receiver)
            .addOnSuccessListener {
                Log.d(TAG, "[Kotlin] [RECEIVE] p2pClient.registerReceiver success")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[Kotlin] [RECEIVE] p2pClient.registerReceiver failure", e)
                onFailure(e)
            }
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

    /**
     * Restores receiver settings (pkgName and fingerPrint) after send operations.
     * This ensures the registered receiver continues to work after sending messages.
     */
    fun restoreReceiverSettings(pkgName: String, fingerPrint: String) {
        p2pClient.setPeerPkgName(pkgName)
        p2pClient.setPeerFingerPrint(fingerPrint)
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
        Log.d(TAG, "[Kotlin] [SEND] send(String) called")
        Log.d(TAG, "[Kotlin] [SEND] Message string length: ${sendMessage.length}")
        Log.d(TAG, "[Kotlin] [SEND] Building message from string")
        val message = buildMessage(sendMessage)
        Log.d(TAG, "[Kotlin] [SEND] Message built, payload size: ${message.data?.size ?: 0} bytes")
        send(
            connectedDevice,
            pkgName,
            fingerPrint,
            message,
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
        Log.d(TAG, "[Kotlin] [SEND] send(Message) called")
        Log.d(TAG, "[Kotlin] [SEND] Device: ${connectedDevice.toMap()}, pkgName: $pkgName, fingerPrint: $fingerPrint")
        Log.d(TAG, "[Kotlin] [SEND] Message payload size: ${sendMessage.data?.size ?: 0} bytes")
        Log.d(TAG, "[Kotlin] [SEND] Setting peer package name: $pkgName")
        p2pClient.setPeerPkgName(pkgName)
        Log.d(TAG, "[Kotlin] [SEND] Setting peer fingerprint: $fingerPrint")
        p2pClient.setPeerFingerPrint(fingerPrint)
        Log.d(TAG, "[Kotlin] [SEND] Calling p2pClient.send")
        p2pClient.send(connectedDevice, sendMessage, sendCallback)
            .addOnSuccessListener{
                Log.d(TAG, "[Kotlin] [SEND] p2pClient.send success")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[Kotlin] [SEND] p2pClient.send failure", e)
                onFailure(e)
            }
    }

    fun buildMessage(message: String): Message {
        Log.d(TAG, "[Kotlin] [SEND] buildMessage(String) - encoding string to bytes")
        val builder = Message.Builder()
        val payload = message.encodeToByteArray()
        Log.d(TAG, "[Kotlin] [SEND] buildMessage(String) - payload size: ${payload.size} bytes")
        builder.setPayload(payload)
        val builtMessage = builder.build()
        Log.d(TAG, "[Kotlin] [SEND] buildMessage(String) - message built successfully")
        return builtMessage
    }

    fun buildMessageFromFile(filePath: String): Message {
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromFile - filePath: $filePath")
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "[Kotlin] [SEND] buildMessageFromFile - File does not exist: $filePath")
            throw IllegalArgumentException("File does not exist: $filePath")
        }
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromFile - File exists, size: ${file.length()} bytes")
        val builder = Message.Builder()
        val payload = file.readBytes()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromFile - Read ${payload.size} bytes from file")
        builder.setPayload(payload)
        val builtMessage = builder.build()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromFile - Message built successfully")
        return builtMessage
    }

    fun buildMessageFromJson(jsonData: Map<String, Any>): Message {
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromJson - jsonData keys: ${jsonData.keys}")
        val jsonObject = JSONObject(jsonData)
        val jsonString = jsonObject.toString()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromJson - JSON string length: ${jsonString.length}")
        val builder = Message.Builder()
        val payload = jsonString.encodeToByteArray()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromJson - payload size: ${payload.size} bytes")
        builder.setPayload(payload)
        val builtMessage = builder.build()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromJson - Message built successfully")
        return builtMessage
    }

    fun buildMessageFromBytes(bytes: ByteArray): Message {
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromBytes - bytes size: ${bytes.size}")
        val builder = Message.Builder()
        builder.setPayload(bytes)
        val builtMessage = builder.build()
        Log.d(TAG, "[Kotlin] [SEND] buildMessageFromBytes - Message built successfully")
        return builtMessage
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
        Log.d(TAG, "[Kotlin] [SEND_FILE] sendFile called - filePath: $filePath")
        try {
            val message = buildMessageFromFile(filePath)
            Log.d(TAG, "[Kotlin] [SEND_FILE] Message built from file, calling send")
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
            Log.e(TAG, "[Kotlin] [SEND_FILE] Exception building message from file", e)
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
        Log.d(TAG, "[Kotlin] [SEND_JSON] sendJson called")
        try {
            val message = buildMessageFromJson(jsonData)
            Log.d(TAG, "[Kotlin] [SEND_JSON] Message built from JSON, calling send")
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
            Log.e(TAG, "[Kotlin] [SEND_JSON] Exception building message from JSON", e)
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
        Log.d(TAG, "[Kotlin] [SEND_BYTES] sendBytes called - bytes size: ${bytes.size}")
        try {
            val message = buildMessageFromBytes(bytes)
            Log.d(TAG, "[Kotlin] [SEND_BYTES] Message built from bytes, calling send")
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
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Exception building message from bytes", e)
            onFailure(e)
        }
    }
}