package com.tablemi.flutter_bluetooth_basic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** FlutterBluetoothBasicPlugin */
public class FlutterBluetoothBasicPlugin implements MethodCallHandler, RequestPermissionsResultListener {
  private static final String TAG = "BluetoothBasicPlugin";
  private int id = 0;
  private ThreadPool threadPool;
  private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
  private static final String NAMESPACE = "flutter_bluetooth_basic";
  private final Registrar registrar;
  private final Activity activity;
  private final MethodChannel channel;
  private final EventChannel stateChannel;
  private final BluetoothManager mBluetoothManager;
  private BluetoothAdapter mBluetoothAdapter;

  private MethodCall pendingCall;
  private Result pendingResult;

  public static void registerWith(Registrar registrar) {
    final FlutterBluetoothBasicPlugin instance = new FlutterBluetoothBasicPlugin(registrar);
    registrar.addRequestPermissionsResultListener(instance);
  }

  FlutterBluetoothBasicPlugin(Registrar r){
    this.registrar = r;
    this.activity = r.activity();
    this.channel = new MethodChannel(registrar.messenger(), NAMESPACE + "/methods");
    this.stateChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/state");
    this.mBluetoothManager = (BluetoothManager) registrar.activity().getSystemService(Context.BLUETOOTH_SERVICE);
    this.mBluetoothAdapter = mBluetoothManager.getAdapter();
    channel.setMethodCallHandler(this);
    stateChannel.setStreamHandler(stateStreamHandler);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (mBluetoothAdapter == null && !"isAvailable".equals(call.method)) {
      result.error("bluetooth_unavailable", "Bluetooth is unavailable", null);
      return;
    }

    final Map<String, Object> args = call.arguments();

    switch (call.method){
      case "state":
        state(result);
        break;
      case "isAvailable":
        result.success(mBluetoothAdapter != null);
        break;
      case "isOn":
        result.success(mBluetoothAdapter.isEnabled());
        break;
      case "isConnected":
        result.success(threadPool != null);
        break;
      case "startScan": {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(
                  activity,
                  new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                  REQUEST_COARSE_LOCATION_PERMISSIONS);
          pendingCall = call;
          pendingResult = result;
          break;
        }
        startScan(call, result);
        break;
      }
      case "stopScan":
        stopScan();
        result.success(null);
        break;
      case "connect":
        connect(result, args);
        break;
      case "disconnect":
        result.success(disconnect());
        break;
      case "destroy":
        result.success(destroy());
        break;
      case "writeData":
        writeData(result, args);
        break;
      default:
        result.notImplemented();
        break;
    }

  }

  private void getDevices(Result result){
    List<Map<String, Object>> devices = new ArrayList<>();
    for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
      Map<String, Object> ret = new HashMap<>();
      ret.put("address", device.getAddress());
      ret.put("name", device.getName());
      ret.put("type", device.getType());
      devices.add(ret);
    }

    result.success(devices);
  }

  private void state(Result result){
    try {
      switch(mBluetoothAdapter.getState()) {
        case BluetoothAdapter.STATE_OFF:
          result.success(BluetoothAdapter.STATE_OFF);
          break;
        case BluetoothAdapter.STATE_ON:
          result.success(BluetoothAdapter.STATE_ON);
          break;
        case BluetoothAdapter.STATE_TURNING_OFF:
          result.success(BluetoothAdapter.STATE_TURNING_OFF);
          break;
        case BluetoothAdapter.STATE_TURNING_ON:
          result.success(BluetoothAdapter.STATE_TURNING_ON);
          break;
        default:
          result.success(0);
          break;
      }
    } catch (SecurityException e) {
      result.error("invalid_argument", "Argument 'address' not found", null);
    }

  }

  private void startScan(MethodCall call, Result result) {
    Log.d(TAG,"start scan ");

    try {
      startScan();
      result.success(null);
    } catch (Exception e) {
      result.error("startScan", e.getMessage(), null);
    }
  }

  private void invokeMethodUIThread(final String name, final BluetoothDevice device) {
    final Map<String, Object> ret = new HashMap<>();
    ret.put("address", device.getAddress());
    ret.put("name", device.getName());
    ret.put("type", device.getType());

    activity.runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                channel.invokeMethod(name, ret);
              }
            });
  }

  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      BluetoothDevice device = result.getDevice();
      if(device != null && device.getName() != null){
        invokeMethodUIThread("ScanResult", device);
      }
    }
  };

  private void startScan() throws IllegalStateException {
    BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
    if(scanner == null) throw new IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?");

    // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    scanner.startScan(null, settings, mScanCallback);
  }

  private void stopScan() {
    BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
    if(scanner != null) scanner.stopScan(mScanCallback);
  }

  private void connect(Result result, Map<String, Object> args) {
    if (args.containsKey("address")) {
      String address = (String) args.get("address");
      disconnect();

      new DeviceConnFactoryManager.Build()
              .setId(id)
              // Set the connection method
              .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
              // Set the connected Bluetooth mac address
              .setMacAddress(address)
              .build();
      // Open port
      threadPool = ThreadPool.getInstantiation();
      threadPool.addSerialTask(new Runnable() {
        @Override
        public void run() {
          DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
        }
      });

      result.success(true);
    } else {
      result.error("invalid_argument", "Argument 'address' not found", null);
    }

  }

  /**
   * Reconnect to recycle the last connected object to avoid memory leaks
   */
 private boolean disconnect() {
    try {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers() != null) {
            if (id >= 0 && id < DeviceConnFactoryManager.getDeviceConnFactoryManagers().length) {
                DeviceConnFactoryManager manager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
                
                if (manager != null) {
                    if (manager.reader != null) {
                        try {
                            manager.reader.cancel();
                        } catch (Exception e) {
                            Log.e(TAG, "Error canceling reader: " + e.getMessage());
                        }
                    }
                    
                    if (manager.mPort != null) {
                        try {
                            manager.mPort.closePort();
                        } catch (Exception e) {
                            Log.e(TAG, "Error closing port: " + e.getMessage());
                        }
                        manager.mPort = null;
                    }
                }
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "Error in disconnect: " + e.getMessage());
    }
    return true;
}

  private boolean destroy() {
    DeviceConnFactoryManager.closeAllPort();
    if (threadPool != null) {
      threadPool.stopThreadPool();
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  private void writeData(Result result, Map<String, Object> args) {
    if (args.containsKey("bytes")) {
        final ArrayList<Integer> bytes = (ArrayList<Integer>) args.get("bytes");

        // ThreadPool for task execution
        threadPool = ThreadPool.getInstantiation();
        
        // CountDownLatch to wait for task completion
        CountDownLatch latch = new CountDownLatch(1);

        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Vector<Byte> vectorData = new Vector<>();
                    for (int i = 0; i < bytes.size(); ++i) {
                        Integer val = bytes.get(i);
                        vectorData.add(Byte.valueOf(Integer.toString(val > 127 ? val - 256 : val)));
                    }

                    // Simulate sending data
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(vectorData);
                } finally {
                    // Ensure latch is counted down even if an exception occurs
                    latch.countDown();
                }
            }
        });

        try {
            // Wait for the task to complete
            latch.await();
            // Task completed, return success
            result.success("Task completed successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.error("task_interrupted", "Task was interrupted", e);
        }
    } else {
        result.error("bytes_empty", "Bytes param is empty", null);
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    if (requestCode == REQUEST_COARSE_LOCATION_PERMISSIONS) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startScan(pendingCall, pendingResult);
      } else {
        pendingResult.error("no_permissions", "This app requires location permissions for scanning", null);
        pendingResult = null;
      }
      return true;
    }
    return false;

  }

  private final StreamHandler stateStreamHandler = new StreamHandler() {
    private EventSink sink;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "stateStreamHandler, current action: " + action);

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
          threadPool = null;
          sink.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
          sink.success(1);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
          threadPool = null;
          sink.success(0);
        }
      }
    };

    @Override
    public void onListen(Object o, EventSink eventSink) {
      sink = eventSink;
      IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
      filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
      filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
      activity.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onCancel(Object o) {
      sink = null;
      activity.unregisterReceiver(mReceiver);
    }
  };



}
