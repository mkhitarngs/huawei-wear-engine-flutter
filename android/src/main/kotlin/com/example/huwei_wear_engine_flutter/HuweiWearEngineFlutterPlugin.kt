package com.example.huwei_wear_engine_flutter

import android.app.Activity
import android.app.Notification.WearableExtender
import android.util.Log
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

private const val TAG = "HwWearEngineFlutter"

/** HuweiWearEngineFlutterPlugin */
class HuweiWearEngineFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private lateinit var wearEngineController: WearEngineController

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "huwei_wear_engine_flutter")
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        wearEngineController = WearEngineController(activity!!)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "hasAvailableDevices" -> onHasAvailableDevices(result)
            "checkPermission" -> onCheckPermission(call, result)
            "checkPermissions" -> onCheckPermissions(call, result)
            "requestPermission" -> onRequestPermissions(call, result)
            "getBondedDevices" -> onGetBondedDevices(result)
            "isAppInstalled" -> TODO("Not implemented")
            "getAppVersion" -> TODO("Not implemented")
            "ping" -> TODO("Not implemented")
            "send" -> TODO("Not implemented")
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
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
            override fun onOk(results: Array<out Permission>?) {
                Log.i(TAG, "Request Permissions - onOk")
//                TODO("Not yet implemented")
            }

            override fun onCancel() {
                Log.i(TAG, "Request Permissions - onCancel")
//                TODO("Not yet implemented")
            }
        }
        val onResult: () -> Unit = {
            Log.i(TAG, "Request Permissions - On Result")
            channelResult.success(listOf(true, true))
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
            channelResult.success(devices)
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Get Bonded Devices - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.getBondedDevices(onResult, onFailure)
    }
}
