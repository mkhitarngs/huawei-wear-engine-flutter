package com.mkhitarngs.flutter.plugin.huawei_wear_engine

import android.os.Looper
import android.util.Log
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.PingCallback
import com.huawei.wearengine.p2p.Receiver
import com.huawei.wearengine.p2p.SendCallback
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel

private const val TAG = "HwWearEngineFlutter"

/** HuaweiWearEngineFlutterPlugin */
class HuaweiWearEngineFlutterPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var wearEngineController: WearEngineController
    private var messageReceiver: Receiver? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "huawei_wear_engine")
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "com.mkhitarngs.flutter.plugin.huawei_wear_engine/wear_engine")
        wearEngineController = WearEngineController(flutterPluginBinding.applicationContext)
        channel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "hasAvailableDevices" -> onHasAvailableDevices(result)
            "checkPermission" -> onCheckPermission(call, result)
            "checkPermissions" -> onCheckPermissions(call, result)
            "requestPermission" -> onRequestPermissions(call, result)
            "getBondedDevices" -> onGetBondedDevices(result)
            "isAppInstalled" -> onIsAppInstalled(call, result)
            "getAppVersion" -> onGetAppVersion(call, result)
            "ping" -> onPing(call, result)
            "send" -> onSend(call, result)
            "sendFile" -> onSendFile(call, result)
            "sendJson" -> onSendJson(call, result)
            "sendBytes" -> onSendBytes(call, result)
            "registerReceiver" -> onRegisterReceiver(call, result)
            "unregisterReceiver" -> onUnregisterReceiver(result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    private fun sendEvent(event: String) {
        eventSink?.success(mapOf("type" to event))
    }

    private fun sendEventWithResult(event: String, result: Any) {
        eventSink?.success(mapOf("type" to event, "result" to result))
    }

    private fun sendEventWithResultAndOpId(event: String, result: Any, opId: Int) {
        eventSink?.success(mapOf("type" to event, "result" to result, "opId" to opId))
    }

    private fun onHasAvailableDevices(channelResult: Result) {
        val onResult: (Boolean) -> Unit = { result: Boolean ->
            Log.i(TAG, "Has Available Devices - On Result")
            channelResult.success(result)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Has Available Devices - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }
        wearEngineController.hasAvailableDevices(onResult = onResult, onFailure = onFailure)
    }

    private fun onCheckPermission(call: MethodCall, channelResult: Result) {
        val strPermission = call.argument<String>("permission")
        val permission: Permission? = strPermission?.toPermission()

        if (permission == null) {
            channelResult.error(TAG, "Permission doesn't exist!!!", null)
            return
        }

        val onResult: (Boolean) -> Unit = { result: Boolean ->
            Log.i(TAG, "Check Permission - On Result")
            channelResult.success(result)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Check Permission - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.checkPermission(permission!!, onResult, onFailure)
    }

    private fun onCheckPermissions(call: MethodCall, channelResult: Result) {
        val strPermissions: List<String>? = call.argument<List<String>>("permissions")
        val permissions = strPermissions
            ?.mapNotNull { permission -> permission.toPermission() }
            ?.toTypedArray()

        if ((permissions?.size ?: 0) == 0) {
            channelResult.error(TAG, "Permissions cannot be empty!!!", null)
            return
        }

        val onResult: (Array<out Boolean>) -> Unit = { result: Array<out Boolean> ->
            Log.i(TAG, "Check Permissions - On Result")
            channelResult.success(result.toList())
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Check Permissions - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.checkPermissions(permissions!!, onResult, onFailure)
    }

    private fun onRequestPermissions(call: MethodCall, channelResult: Result) {
        val strPermissions: List<String>? = call.argument<List<String>>("permissions")
        val permissions = strPermissions
            ?.mapNotNull { permission -> permission.toPermission() }
            ?.toTypedArray()

        if ((permissions?.size ?: 0) == 0) {
            channelResult.error(TAG, "Permissions cannot be empty!!!", null)
            return
        }

        val authCallback: AuthCallback = object : AuthCallback {
            override fun onOk(grantedPermissions: Array<out Permission>?) {
                val result: List<String> = grantedPermissions?.map { it.name }?.toList()?: listOf()

                Log.i(TAG, "Request Permissions - onOk")
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    sendEventWithResult("onOk", result)
                }
            }

            override fun onCancel() {
                Log.i(TAG, "Request Permissions - onCancel")
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                sendEvent("onCancel")
                }
            }
        }
        val onResult: () -> Unit = {
            Log.i(TAG, "Request Permissions - On Result")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Request Permissions - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.requestPermission(
            permission = permissions!!,
            authCallback = authCallback,
            onSuccess = onResult,
            onFailure = onFailure
        )
    }

    private fun onGetBondedDevices(channelResult: Result) {
        val onResult: (List<out Device>) -> Unit = { devices: List<Device> ->
            Log.i(TAG, "Get Bonded Devices - On Result")
            channelResult.success(devices.map { it.toMap() })
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Get Bonded Devices - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.getBondedDevices(onResult, onFailure)
    }

    private fun onIsAppInstalled(call: MethodCall, channelResult: Result) {
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")

        if (mpDevice?.isEmpty() != false) {
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }

        val device: Device = mapToDevice(mpDevice!!)
        val onResult: (Boolean) -> Unit = { result ->
            Log.i(TAG, "Is App Installed - On Result")
            channelResult.success(result)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Is App Installed - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.isAppInstalled(device, pkgName!!, onResult, onFailure)
    }

    private fun onGetAppVersion(call: MethodCall, channelResult: Result) {
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")

        if (mpDevice?.isEmpty() != false) {
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }

        val device: Device = mapToDevice(mpDevice!!)
        val onResult: (Int) -> Unit = { result ->
            Log.i(TAG, "Get App Version - On Result")
            channelResult.success(result)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Get App Version - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.getAppVersion(device, pkgName!!, onResult, onFailure)
    }

    private fun onPing(call: MethodCall, channelResult: Result) {
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")

        if (mpDevice?.isEmpty() != false) {
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }

        val device: Device = mapToDevice(mpDevice!!)
        val pingCallback: PingCallback = PingCallback { result ->
            android.os.Handler(
                Looper.getMainLooper()
            ).post {
                Log.i(TAG, "Ping - On Ping Result")
                sendEventWithResult("onPingResult", result)
            }
        }
        val onSent: () -> Unit = {
            Log.i(TAG, "Ping - On Sent")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Ping - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.ping(device, pkgName!!, pingCallback, onSent, onFailure)
    }

    private fun onSend(call: MethodCall, channelResult: Result) {
        Log.d(TAG, "[Kotlin] [SEND] onSend method called")
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")
        val strMessage: String? = call.argument<String>("message")
        val opId: Int? = call.argument<Int>("opId")

        Log.d(TAG, "[Kotlin] [SEND] Extracted parameters - pkgName: $pkgName, fingerPrint: $fingerPrint, messageLength: ${strMessage?.length}, opId: $opId")

        if (mpDevice?.isEmpty() != false) {
            Log.e(TAG, "[Kotlin] [SEND] Validation failed: Device cannot be empty")
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND] Validation failed: Package name cannot be empty")
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }
        if (fingerPrint.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND] Validation failed: Finger print cannot be empty")
            channelResult.error(TAG, "Finger print name cannot be empty!!!", null)
            return
        }
        if (strMessage.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND] Validation failed: Message cannot be empty")
            channelResult.error(TAG, "Message name cannot be empty!!!", null)
            return
        }
        if (opId == null) {
            Log.e(TAG, "[Kotlin] [SEND] Validation failed: Operation ID cannot be null")
            channelResult.error(TAG, "Operation ID cannot be null!!!", null)
            return
        }

        Log.d(TAG, "[Kotlin] [SEND] All validations passed")
        val device: Device = mapToDevice(mpDevice!!)
        Log.d(TAG, "[Kotlin] [SEND] Device mapped: ${device.toMap()}")

        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(codeResult: Int) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND] onSendResult callback - code: $codeResult, opId: $opId")
                    sendEventWithResultAndOpId("onSendResult", codeResult, opId)
                    Log.d(TAG, "[Kotlin] [SEND] onSendResult event sent to Flutter")
                }
            }

            override fun onSendProgress(progress: Long) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND] onSendProgress callback - progress: $progress, opId: $opId")
                    sendEventWithResultAndOpId("onSendProgress", progress, opId)
                    Log.d(TAG, "[Kotlin] [SEND] onSendProgress event sent to Flutter")
                }
            }
        }
        val onSend: () -> Unit = {
            Log.i(TAG, "[Kotlin] [SEND] onSend success callback")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "[Kotlin] [SEND] onFailure callback", e)
            channelResult.error(TAG, e.message, null)
        }

        Log.d(TAG, "[Kotlin] [SEND] Calling wearEngineController.send")
        wearEngineController.send(
            device,
            pkgName!!,
            fingerPrint!!,
            strMessage!!,
            sendCallback,
            onSend,
            onFailure
        )
        Log.d(TAG, "[Kotlin] [SEND] wearEngineController.send call completed")
    }

    private fun onSendFile(call: MethodCall, channelResult: Result) {
        Log.d(TAG, "[Kotlin] [SEND_FILE] onSendFile method called")
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")
        val filePath: String? = call.argument<String>("filePath")
        val opId: Int? = call.argument<Int>("opId")

        Log.d(TAG, "[Kotlin] [SEND_FILE] Extracted parameters - pkgName: $pkgName, fingerPrint: $fingerPrint, filePath: $filePath, opId: $opId")

        if (mpDevice?.isEmpty() != false) {
            Log.e(TAG, "[Kotlin] [SEND_FILE] Validation failed: Device cannot be empty")
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_FILE] Validation failed: Package name cannot be empty")
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }
        if (fingerPrint.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_FILE] Validation failed: Finger print cannot be empty")
            channelResult.error(TAG, "Finger print name cannot be empty!!!", null)
            return
        }
        if (filePath.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_FILE] Validation failed: File path cannot be empty")
            channelResult.error(TAG, "File path cannot be empty!!!", null)
            return
        }
        if (opId == null) {
            Log.e(TAG, "[Kotlin] [SEND_FILE] Validation failed: Operation ID cannot be null")
            channelResult.error(TAG, "Operation ID cannot be null!!!", null)
            return
        }

        Log.d(TAG, "[Kotlin] [SEND_FILE] All validations passed")
        val device: Device = mapToDevice(mpDevice!!)
        Log.d(TAG, "[Kotlin] [SEND_FILE] Device mapped: ${device.toMap()}")

        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(codeResult: Int) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_FILE] onSendResult callback - code: $codeResult, opId: $opId")
                    sendEventWithResultAndOpId("onSendResult", codeResult, opId)
                    Log.d(TAG, "[Kotlin] [SEND_FILE] onSendResult event sent to Flutter")
                }
            }

            override fun onSendProgress(progress: Long) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_FILE] onSendProgress callback - progress: $progress, opId: $opId")
                    sendEventWithResultAndOpId("onSendProgress", progress, opId)
                    Log.d(TAG, "[Kotlin] [SEND_FILE] onSendProgress event sent to Flutter")
                }
            }
        }
        val onSend: () -> Unit = {
            Log.i(TAG, "[Kotlin] [SEND_FILE] onSend success callback")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "[Kotlin] [SEND_FILE] onFailure callback", e)
            channelResult.error(TAG, e.message, null)
        }

        Log.d(TAG, "[Kotlin] [SEND_FILE] Calling wearEngineController.sendFile")
        wearEngineController.sendFile(
            device,
            pkgName!!,
            fingerPrint!!,
            filePath!!,
            sendCallback,
            onSend,
            onFailure
        )
        Log.d(TAG, "[Kotlin] [SEND_FILE] wearEngineController.sendFile call completed")
    }

    private fun onSendJson(call: MethodCall, channelResult: Result) {
        Log.d(TAG, "[Kotlin] [SEND_JSON] onSendJson method called")
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")
        val jsonData: Map<String, Any>? = call.argument<Map<String, Any>>("jsonData")
        val opId: Int? = call.argument<Int>("opId")

        Log.d(TAG, "[Kotlin] [SEND_JSON] Extracted parameters - pkgName: $pkgName, fingerPrint: $fingerPrint, jsonDataKeys: ${jsonData?.keys}, opId: $opId")

        if (mpDevice?.isEmpty() != false) {
            Log.e(TAG, "[Kotlin] [SEND_JSON] Validation failed: Device cannot be empty")
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_JSON] Validation failed: Package name cannot be empty")
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }
        if (fingerPrint.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_JSON] Validation failed: Finger print cannot be empty")
            channelResult.error(TAG, "Finger print name cannot be empty!!!", null)
            return
        }
        if (jsonData == null || jsonData.isEmpty()) {
            Log.e(TAG, "[Kotlin] [SEND_JSON] Validation failed: JSON data cannot be empty")
            channelResult.error(TAG, "JSON data cannot be empty!!!", null)
            return
        }
        if (opId == null) {
            Log.e(TAG, "[Kotlin] [SEND_JSON] Validation failed: Operation ID cannot be null")
            channelResult.error(TAG, "Operation ID cannot be null!!!", null)
            return
        }

        Log.d(TAG, "[Kotlin] [SEND_JSON] All validations passed")
        val device: Device = mapToDevice(mpDevice!!)
        Log.d(TAG, "[Kotlin] [SEND_JSON] Device mapped: ${device.toMap()}")

        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(codeResult: Int) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_JSON] onSendResult callback - code: $codeResult, opId: $opId")
                    sendEventWithResultAndOpId("onSendResult", codeResult, opId)
                    Log.d(TAG, "[Kotlin] [SEND_JSON] onSendResult event sent to Flutter")
                }
            }

            override fun onSendProgress(progress: Long) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_JSON] onSendProgress callback - progress: $progress, opId: $opId")
                    sendEventWithResultAndOpId("onSendProgress", progress, opId)
                    Log.d(TAG, "[Kotlin] [SEND_JSON] onSendProgress event sent to Flutter")
                }
            }
        }
        val onSend: () -> Unit = {
            Log.i(TAG, "[Kotlin] [SEND_JSON] onSend success callback")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "[Kotlin] [SEND_JSON] onFailure callback", e)
            channelResult.error(TAG, e.message, null)
        }

        Log.d(TAG, "[Kotlin] [SEND_JSON] Calling wearEngineController.sendJson")
        wearEngineController.sendJson(
            device,
            pkgName!!,
            fingerPrint!!,
            jsonData!!,
            sendCallback,
            onSend,
            onFailure
        )
        Log.d(TAG, "[Kotlin] [SEND_JSON] wearEngineController.sendJson call completed")
    }

    private fun onSendBytes(call: MethodCall, channelResult: Result) {
        Log.d(TAG, "[Kotlin] [SEND_BYTES] onSendBytes method called")
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")
        val bytes: List<Int>? = call.argument<List<Int>>("bytes")
        val opId: Int? = call.argument<Int>("opId")

        Log.d(TAG, "[Kotlin] [SEND_BYTES] Extracted parameters - pkgName: $pkgName, fingerPrint: $fingerPrint, bytesLength: ${bytes?.size}, opId: $opId")

        if (mpDevice?.isEmpty() != false) {
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Validation failed: Device cannot be empty")
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Validation failed: Package name cannot be empty")
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }
        if (fingerPrint.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Validation failed: Finger print cannot be empty")
            channelResult.error(TAG, "Finger print name cannot be empty!!!", null)
            return
        }
        if (bytes == null || bytes.isEmpty()) {
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Validation failed: Bytes cannot be empty")
            channelResult.error(TAG, "Bytes cannot be empty!!!", null)
            return
        }
        if (opId == null) {
            Log.e(TAG, "[Kotlin] [SEND_BYTES] Validation failed: Operation ID cannot be null")
            channelResult.error(TAG, "Operation ID cannot be null!!!", null)
            return
        }

        Log.d(TAG, "[Kotlin] [SEND_BYTES] All validations passed")
        val device: Device = mapToDevice(mpDevice!!)
        Log.d(TAG, "[Kotlin] [SEND_BYTES] Device mapped: ${device.toMap()}")

        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(codeResult: Int) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_BYTES] onSendResult callback - code: $codeResult, opId: $opId")
                    sendEventWithResultAndOpId("onSendResult", codeResult, opId)
                    Log.d(TAG, "[Kotlin] [SEND_BYTES] onSendResult event sent to Flutter")
                }
            }

            override fun onSendProgress(progress: Long) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "[Kotlin] [SEND_BYTES] onSendProgress callback - progress: $progress, opId: $opId")
                    sendEventWithResultAndOpId("onSendProgress", progress, opId)
                    Log.d(TAG, "[Kotlin] [SEND_BYTES] onSendProgress event sent to Flutter")
                }
            }
        }
        val onSend: () -> Unit = {
            Log.i(TAG, "[Kotlin] [SEND_BYTES] onSend success callback")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "[Kotlin] [SEND_BYTES] onFailure callback", e)
            channelResult.error(TAG, e.message, null)
        }

        Log.d(TAG, "[Kotlin] [SEND_BYTES] Converting bytes list to byte array")
        val byteArray = bytes!!.map { it.toByte() }.toByteArray()
        Log.d(TAG, "[Kotlin] [SEND_BYTES] Byte array created, length: ${byteArray.size}")
        Log.d(TAG, "[Kotlin] [SEND_BYTES] Calling wearEngineController.sendBytes")
        wearEngineController.sendBytes(
            device,
            pkgName!!,
            fingerPrint!!,
            byteArray,
            sendCallback,
            onSend,
            onFailure
        )
        Log.d(TAG, "[Kotlin] [SEND_BYTES] wearEngineController.sendBytes call completed")
    }

    private fun onRegisterReceiver(call: MethodCall, channelResult: Result) {
        Log.d(TAG, "[Kotlin] [RECEIVE] onRegisterReceiver method called")
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")

        Log.d(TAG, "[Kotlin] [RECEIVE] Extracted parameters - pkgName: $pkgName, fingerPrint: $fingerPrint")

        if (mpDevice?.isEmpty() != false) {
            Log.e(TAG, "[Kotlin] [RECEIVE] Validation failed: Device cannot be empty")
            channelResult.error(TAG, "Device cannot be empty!!!", null)
            return
        }
        if (pkgName.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [RECEIVE] Validation failed: Package name cannot be empty")
            channelResult.error(TAG, "Package name cannot be empty!!!", null)
            return
        }
        if (fingerPrint.isNullOrBlank()) {
            Log.e(TAG, "[Kotlin] [RECEIVE] Validation failed: Finger print cannot be empty")
            channelResult.error(TAG, "Finger print cannot be empty!!!", null)
            return
        }

        Log.d(TAG, "[Kotlin] [RECEIVE] All validations passed")
        val device: Device = mapToDevice(mpDevice!!)
        Log.d(TAG, "[Kotlin] [RECEIVE] Device mapped: ${device.toMap()}")

        if (messageReceiver == null) {
            Log.d(TAG, "[Kotlin] [RECEIVE] Creating new message receiver")
            messageReceiver = Receiver { message ->
                android.os.Handler(Looper.getMainLooper()).post {
                    Log.i(TAG, "[Kotlin] [RECEIVE] MessageReceiver.onReceive called")
                    Log.d(TAG, "[Kotlin] [RECEIVE] Message payload size: ${message?.data?.size ?: 0} bytes")
                    if (eventSink == null) {
                        Log.e(TAG, "[Kotlin] [RECEIVE] EventSink is null! Cannot send message to Flutter")
                    } else {
                        val messageMap = message.toMap()
                        Log.d(TAG, "[Kotlin] [RECEIVE] Converting message to map: payloadSize=${messageMap["payload"]?.let { (it as? ByteArray)?.size }}")
                        sendEventWithResult("onMessageReceived", messageMap)
                        Log.d(TAG, "[Kotlin] [RECEIVE] Message event sent to Flutter successfully")
                    }
                }
            }
            Log.d(TAG, "[Kotlin] [RECEIVE] Message receiver created")
        } else {
            Log.d(TAG, "[Kotlin] [RECEIVE] Using existing message receiver")
        }

        val onResult: () -> Unit = {
            Log.i(TAG, "[Kotlin] [RECEIVE] Register Receiver - On Result success")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "[Kotlin] [RECEIVE] Register Receiver - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        Log.d(TAG, "[Kotlin] [RECEIVE] Calling wearEngineController.registerReceiver")
        wearEngineController.registerReceiver(
            device,
            pkgName!!,
            fingerPrint!!,
            messageReceiver!!,
            onResult,
            onFailure
        )
        Log.d(TAG, "[Kotlin] [RECEIVE] wearEngineController.registerReceiver call completed")
    }

    private fun onUnregisterReceiver(channelResult: Result) {
        if (messageReceiver == null) {
            channelResult.success(null)
            return
        }

        val onResult: () -> Unit = {
            Log.i(TAG, "Unregister Receiver - On Result")
            messageReceiver = null
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Unregister Receiver - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.unregisterReceiver(messageReceiver!!, onResult, onFailure)
    }
}