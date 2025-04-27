package com.tablemi.flutter_bluetooth_basic

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterBluetoothBasicPlugin */
class FlutterBluetoothBasicPlugin: FlutterPlugin {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      // This is a helper function that maintains backward compatibility
      // with apps that don't use the v2 embedding
      if (registrar.activity() != null) {
        // Use the Java implementation which handles the old embedding
        com.tablemi.flutter_bluetooth_basic.FlutterBluetoothBasicPluginOld.registerWith(registrar)
      }
    }
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // Create a new instance of the plugin and attach it to the engine
    val plugin = com.tablemi.flutter_bluetooth_basic.FlutterBluetoothBasicPlugin()
    plugin.onAttachedToEngine(binding)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // No additional teardown needed
  }
}