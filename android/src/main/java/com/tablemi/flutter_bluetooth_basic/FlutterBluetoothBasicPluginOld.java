package com.tablemi.flutter_bluetooth_basic;

import android.content.Context;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterBluetoothBasicPluginOld {
  private static final String NAMESPACE = "flutter_bluetooth_basic";

  /**
   * Plugin registration for apps using the old embedding (V1).
   */
  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      // When a background flutter view tries to register the plugin, the registrar has no activity.
      // We avoid crashing the app in that case.
      return;
    }
    
    final MethodChannel channel = new MethodChannel(registrar.messenger(), NAMESPACE + "/methods");
    final EventChannel stateChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/state");
    
    FlutterBluetoothBasicPlugin plugin = new FlutterBluetoothBasicPlugin();
    
    // Initialize channels and plugin manually for V1 embedding
    plugin.context = registrar.context();
    plugin.activity = registrar.activity();
    plugin.channel = channel;
    plugin.stateChannel = stateChannel;
    
    // Setup Bluetooth components
    plugin.setupBluetoothAdapter();
    
    // Set method call handler and stream handler
    channel.setMethodCallHandler(plugin);
    stateChannel.setStreamHandler(plugin.stateStreamHandler);
    
    // Add permissions result listener
    registrar.addRequestPermissionsResultListener(plugin);
  }
}