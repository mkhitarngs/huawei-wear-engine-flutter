package com.example.huwei_wear_engine_flutter

import android.app.Notification.WearableExtender
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** HuweiWearEngineFlutterPlugin */
class HuweiWearEngineFlutterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var wearEngineController: WearEngineController

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "huwei_wear_engine_flutter")
    wearEngineController = WearEngineController(flutterPluginBinding.getApplicationContext())
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }
    else if(call.method == "has") {
      onHasAvailableDevices(result)
    }
    else {
      result.notImplemented()
    }
  }

  private fun onHasAvailableDevices(channelResult: Result) {
    val onResult:(Boolean) -> Unit =  { result: Boolean ->
      Log.e("Oliver404", "No error")
      channelResult.success(result)
    }
    val onFailure = { e: Exception ->
      Log.e("Oliver404", "error", e)
      channelResult.error("WearEngine", e.message, null)
    }
    wearEngineController.hasAvailableDevices(onResult = onResult, onFailure = onFailure)

  }


  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
