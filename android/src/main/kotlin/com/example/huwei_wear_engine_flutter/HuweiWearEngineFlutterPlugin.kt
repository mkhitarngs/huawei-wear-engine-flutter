package com.example.huwei_wear_engine_flutter

import android.app.Notification.WearableExtender
import android.util.Log
import com.huawei.wearengine.auth.Permission
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

private const val TAG = "HwWearEngineFlutter"

/** HuweiWearEngineFlutterPlugin */
class HuweiWearEngineFlutterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var wearEngineController: WearEngineController

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "huwei_wear_engine_flutter")
        wearEngineController = WearEngineController(flutterPluginBinding.getApplicationContext())
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "hasAvailableDevices" -> onHasAvailableDevices(result)
            "checkPermission" -> onCheckPermission(call, result)
            "checkPermissions" -> onCheckPermissions(call, result)
            "requestPermission" -> TODO("Not implemented")
            "getBondedDevices" -> TODO("Not implemented")
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

        val onResult: (Array<out Boolean>) -> Unit = { result: Array<out Boolean> ->
            Log.i(TAG, "Check Permissions - On Result")
            channelResult.success(result.toList())
        }
        val onFailure: (Exception) -> Unit = { e: Exception ->
            Log.e(TAG, "Check Permissions - On Failure", e)
            channelResult.error(TAG, e.message, null)
        }

        wearEngineController.requestPermission(*permissions!!)
    }
}
