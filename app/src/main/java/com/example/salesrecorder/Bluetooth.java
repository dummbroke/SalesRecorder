package com.example.salesrecorder;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Bluetooth extends AppCompatActivity {
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 201;
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

                    onBluetoothConnected();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Bluetooth", "Failed to connect to the device", e);
                    Toast.makeText(this, "Failed to connect to the device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonScan.setOnClickListener(view -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                // Check if the location permission is granted (both fine and coarse)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                } else {
                    startBluetoothDiscovery();
                }
            } else {
                Toast.makeText(this, "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        });

        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                switchBluetooth.setChecked(false);
                return;
            }

            if (isChecked) {
                if (!bluetoothAdapter.isEnabled()) {
                    // For Android 12 and above, BLUETOOTH_CONNECT permission is required
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                    } else {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }
            } else {
                // For disabling Bluetooth, also check for BLUETOOTH_CONNECT permission on Android 12 and above
                if (bluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "BLUETOOTH_CONNECT permission not granted. Cannot disable Bluetooth.", Toast.LENGTH_SHORT).show();
                    } else {
                        bluetoothAdapter.disable();
                    }
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
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); // You can also use this for filtering by MAC address

                    // Filter to only add HC-05 devices to the list
                    if (deviceName != null && deviceName.startsWith("HC-05")) {
                        String deviceInfo = deviceName + "\n" + deviceAddress;
                        if (!discoveredDevices.contains(deviceInfo)) {
                            discoveredDevices.add(deviceInfo);
                            discoveredDevicesArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle the result of our permission request
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Location permission is required to discover Bluetooth devices", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startBluetoothDiscovery() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startDiscovery();
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
    public void sendData(String mainProductName, List<String> subProducts) {
        // Combine main product name and sub-products into a single string
        // Format: "MainProduct:SubProduct1,SubProduct2,SubProduct3;"
        StringBuilder formattedDataBuilder = new StringBuilder();
        formattedDataBuilder.append(mainProductName).append(":");

        for (int i = 0; i < subProducts.size(); i++) {
            formattedDataBuilder.append(subProducts.get(i));
            if (i < subProducts.size() - 1) {
                formattedDataBuilder.append(",");
            }
        }
        formattedDataBuilder.append(";"); // End of main product entry

        String formattedData = formattedDataBuilder.toString() + "\n"; // Add newline for transmission end

        Log.d("Bluetooth", "Formatted data to send: " + formattedData);

        if (bluetoothSocket != null) {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                byte[] dataToSend = formattedData.getBytes();
                outputStream.write(dataToSend); // Send the formatted data
                Toast.makeText(this, "Data sent: " + formattedData, Toast.LENGTH_SHORT).show();
                Log.d("Bluetooth", "Data successfully sent to the device: " + formattedData);
            } catch (IOException e) {
                Log.e("Bluetooth", "Error when sending data", e);
                Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
            Log.e("Bluetooth", "Attempted to send data but Bluetooth is not connected.");
        }
    }

    private void onBluetoothConnected() {
        fetchAndSendProducts();
    }
    private void fetchAndSendProducts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).collection("productCategories")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot mainProductDoc : task.getResult()) {
                                String mainProductName = mainProductDoc.getString("name");
                                fetchSubProducts(mainProductName, subProducts -> {
                                    sendData(mainProductName, subProducts);
                                });
                            }
                        } else {
                            Log.e("Bluetooth", "Error getting main categories: ", task.getException());
                        }
                    });
        } else {
            Log.d("Bluetooth", "No user logged in.");
        }
    }

    private void fetchSubProducts(String mainProductName, Consumer<List<String>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("productCategories").whereEqualTo("name", mainProductName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String categoryId = task.getResult().getDocuments().get(0).getId();
                        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("productCategories").document(categoryId)
                                .collection("subProducts").get()
                                .addOnCompleteListener(subTask -> {
                                    List<String> subProductNames = new ArrayList<>();
                                    if (subTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : subTask.getResult()) {
                                            String subProductName = document.getString("name");
                                            subProductNames.add(subProductName);
                                        }
                                    }
                                    callback.accept(subProductNames);
                                });
                    }
                });
    }
}