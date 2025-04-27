package com.tablemi.flutter_bluetooth_basic;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterBluetoothBasicPluginOld {
  /**
   * Plugin registration for apps using the old embedding.
   * This maintains backward compatibility for applications
   * that haven't migrated to the new embedding yet.
   */
  public static void registerWith(Registrar registrar) {
    FlutterBluetoothBasicPlugin plugin = new FlutterBluetoothBasicPlugin();
    plugin.onAttachedToEngine(getFlutterPluginBinding(registrar));
    plugin.onAttachedToActivity(getActivityPluginBinding(registrar));
  }

  // Helper methods to create binding objects from the Registrar
  private static class RegistrarFlutterPluginBinding implements FlutterPlugin.FlutterPluginBinding {
    private final Registrar registrar;

    RegistrarFlutterPluginBinding(Registrar registrar) {
      this.registrar = registrar;
    }

    @Override
    public io.flutter.plugin.common.BinaryMessenger getBinaryMessenger() {
      return registrar.messenger();
    }

    @Override
    public io.flutter.plugin.common.PluginRegistry getPluginRegistry() {
      throw new UnsupportedOperationException("getPluginRegistry() is not supported");
    }

    @Override
    public android.content.Context getApplicationContext() {
      return registrar.context().getApplicationContext();
    }

    @Override
    public io.flutter.view.FlutterMain.FlutterAssets getFlutterAssets() {
      throw new UnsupportedOperationException("getFlutterAssets() is not supported");
    }

    @Override
    public String getFlutterView() {
      throw new UnsupportedOperationException("getFlutterView() is not supported");
    }
  }

  // Helper method to create FlutterPluginBinding from Registrar
  private static FlutterPlugin.FlutterPluginBinding getFlutterPluginBinding(Registrar registrar) {
    return new RegistrarFlutterPluginBinding(registrar);
  }

  // Helper class to simulate ActivityPluginBinding from Registrar
  private static class RegistrarActivityPluginBinding implements io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding {
    private final Registrar registrar;

    RegistrarActivityPluginBinding(Registrar registrar) {
      this.registrar = registrar;
    }

    @Override
    public android.app.Activity getActivity() {
      return registrar.activity();
    }

    @Override
    public Object getLifecycle() {
      throw new UnsupportedOperationException("getLifecycle() is not supported");
    }

    @Override
    public void addRequestPermissionsResultListener(io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener listener) {
      registrar.addRequestPermissionsResultListener(listener);
    }

    @Override
    public void removeRequestPermissionsResultListener(io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener listener) {
      // No way to remove listeners in V1
    }

    @Override
    public void addActivityResultListener(io.flutter.plugin.common.PluginRegistry.ActivityResultListener listener) {
      registrar.addActivityResultListener(listener);
    }

    @Override
    public void removeActivityResultListener(io.flutter.plugin.common.PluginRegistry.ActivityResultListener listener) {
      // No way to remove listeners in V1
    }

    @Override
    public void addOnNewIntentListener(io.flutter.plugin.common.PluginRegistry.NewIntentListener listener) {
      registrar.addNewIntentListener(listener);
    }

    @Override
    public void removeOnNewIntentListener(io.flutter.plugin.common.PluginRegistry.NewIntentListener listener) {
      // No way to remove listeners in V1
    }

    @Override
    public void addOnUserLeaveHintListener(io.flutter.plugin.common.PluginRegistry.UserLeaveHintListener listener) {
      registrar.addUserLeaveHintListener(listener);
    }

    @Override
    public void removeOnUserLeaveHintListener(io.flutter.plugin.common.PluginRegistry.UserLeaveHintListener listener) {
      // No way to remove listeners in V1
    }

    @Override
    public void addOnSaveStateListener(io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding.OnSaveInstanceStateListener listener) {
      // Not available in V1
    }

    @Override
    public void removeOnSaveStateListener(io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding.OnSaveInstanceStateListener listener) {
      // Not available in V1
    }
  }

  // Helper method to create ActivityPluginBinding from Registrar
  private static io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding getActivityPluginBinding(Registrar registrar) {
    return new RegistrarActivityPluginBinding(registrar);
  }
}