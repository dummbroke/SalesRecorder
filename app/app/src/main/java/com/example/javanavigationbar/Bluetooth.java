package com.example.javanavigationbar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    private ArrayAdapter<String> discoveredDevicesArrayAdapter;
    private final ArrayList<String> discoveredDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothSocket = BluetoothSocketSingleton.getInstance().getBluetoothSocket();
        Log.d("Bluetooth", "Bluetooth socket: " + bluetoothSocket);

        Switch switchBluetooth = findViewById(R.id.switchBluetooth);
        Button buttonScan = findViewById(R.id.buttonScan);
        ListView listDevices = findViewById(R.id.listDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            switchBluetooth.setChecked(bluetoothAdapter.isEnabled());
        }

        discoveredDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listDevices.setAdapter(discoveredDevicesArrayAdapter);

        listDevices.setOnItemClickListener((parent, view, position, id) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            if (address.equals("00:22:09:01:94:D0")) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    bluetoothSocket.connect();

                    BluetoothSocketSingleton.getInstance().setBluetoothSocket(bluetoothSocket);

                    Toast.makeText(this, "Successfully connected to the device", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, BluetoothService.class);
                    startService(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Bluetooth", "Failed to connect to the device", e);
                    Toast.makeText(this, "Failed to connect to the device", Toast.LENGTH_SHORT).show();
                }
            }
        });



        buttonScan.setOnClickListener(view -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                } else {
                    bluetoothAdapter.startDiscovery();
                }
            } else {
                Toast.makeText(this, "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        });

        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                } else {
                    Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {
                    String deviceName = device.getName() + "\n" + device.getAddress();
                    if (!discoveredDevices.contains(deviceName)) {
                        discoveredDevices.add(deviceName);
                        discoveredDevicesArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        } else {
            Toast.makeText(this, "Permission needed for scanning Bluetooth devices", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }
}
