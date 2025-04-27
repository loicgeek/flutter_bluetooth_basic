package com.tablemi.flutter_bluetooth_basic

// This file is used by the Flutter tool to generate GeneratedPluginRegistrant.java
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
    // The FlutterBluetoothBasicPlugin implementation already takes care of
    // initializing the plugin for both V1 and V2 embeddings
    com.tablemi.flutter_bluetooth_basic.FlutterBluetoothBasicPlugin().onAttachedToEngine(binding)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // No need to do anything here, the Java implementation will handle cleanup
  }
}