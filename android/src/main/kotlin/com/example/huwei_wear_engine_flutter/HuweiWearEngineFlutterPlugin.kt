package com.example.huwei_wear_engine_flutter

import android.app.Activity
import android.app.Notification.WearableExtender
import android.os.Looper
import android.util.Log
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.PingCallback
import com.huawei.wearengine.p2p.SendCallback
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel
import java.util.logging.Handler

private const val TAG = "HwWearEngineFlutter"

/** HuweiWearEngineFlutterPlugin */
class HuweiWearEngineFlutterPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var wearEngineController: WearEngineController

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "huwei_wear_engine_flutter")
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "com.example.huwei_wear_engine_flutter/wear_engine")
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

        if (permission == null) channelResult.error(TAG, "Permission dose n't exist!!!", null)

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

        if ((permissions?.size ?: 0) == 0)
            channelResult.error(TAG, "Permissions cannot be empty!!!", null)

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

        if ((permissions?.size ?: 0) == 0)
            channelResult.error(TAG, "Permissions cannot be empty!!!", null)

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

        if (mpDevice?.isEmpty() != false) channelResult.error(TAG, "Device cannot be empty!!!", null)
        if (pkgName.isNullOrBlank()) channelResult.error(TAG, "Package name cannot be empty!!!", null)

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

        if (mpDevice?.isEmpty() != false) channelResult.error(TAG, "Device cannot be empty!!!", null)
        if (pkgName.isNullOrBlank()) channelResult.error(TAG, "Package name cannot be empty!!!", null)

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

        if (mpDevice?.isEmpty() != false) channelResult.error(TAG, "Device cannot be empty!!!", null)
        if (pkgName.isNullOrBlank()) channelResult.error(TAG, "Package name cannot be empty!!!", null)

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
        val mpDevice: Map<String, Any>? = call.argument<Map<String, Any>>("device")
        val pkgName: String? = call.argument<String>("pkgName")
        val fingerPrint: String? = call.argument<String>("fingerPrint")
        val strMessage: String? = call.argument<String>("message")

        if (mpDevice?.isEmpty() != false) channelResult.error(TAG, "Device cannot be empty!!!", null)
        if (pkgName.isNullOrBlank()) channelResult.error(TAG, "Package name cannot be empty!!!", null)
        if (fingerPrint.isNullOrBlank()) channelResult.error(TAG, "Finger print name cannot be empty!!!", null)
        if (strMessage.isNullOrBlank()) channelResult.error(TAG, "Message name cannot be empty!!!", null)

        val device: Device = mapToDevice(mpDevice!!)
        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(codeResult: Int) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "Send - On Send Result")
                    sendEventWithResult("onSendResult", codeResult)
                }
            }

            override fun onSendProgress(progress: Long) {
                android.os.Handler(
                    Looper.getMainLooper()
                ).post {
                    Log.i(TAG, "Send - On Send Progress")
                    sendEventWithResult("onSendProgress", progress)
                }
            }
        }
        val onSend: () -> Unit = {
            Log.i(TAG, "Send - On Send")
            channelResult.success(null)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Send - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.send(
            device,
            pkgName!!,
            fingerPrint!!,
            strMessage!!,
            sendCallback,
            onSend,
            onFailure
        )
    }
}
