package com.example.salesrecorder;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                handleBluetoothStateChange(state);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Register BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        Log.d(TAG, "Service started");
        bluetoothSocket = BluetoothSocketSingleton.getInstance().getBluetoothSocket();

        if (bluetoothSocket != null) {
            try {
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                stopSelf();
                return;
            }
        } else {
            Log.e(TAG, "Failed to retrieve Bluetooth socket");
            stopSelf();
            return;
        }

        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes == -1) { // End of stream reached
                        break;
                    }
                    String readMessage = new String(buffer, 0, bytes);
                    processData(readMessage);
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }

            // Clean-up code here
            cleanup(); // Implement this method to close sockets and clean up resources
        }).start();
    }
    private void handleBluetoothStateChange(int state) {
        if (state == BluetoothAdapter.STATE_OFF) {
            // Stop the service or handle as appropriate
            stopSelf();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver
        unregisterReceiver(bluetoothStateReceiver);
    }
    private void cleanup() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            // Other cleanup actions...
            Log.d(TAG, "Cleaned up resources");
        } catch (IOException e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }
    private final StringBuilder dataBuffer = new StringBuilder();

    private void processData(String data) {
        Log.d(TAG, "Processing data: " + data);
        dataBuffer.append(data);

        // Process each complete line in the buffer
        while (dataBuffer.indexOf("\n") != -1) {
            String completeMessage = dataBuffer.substring(0, dataBuffer.indexOf("\n")).trim();
            dataBuffer.delete(0, dataBuffer.indexOf("\n") + 1); // Remove the processed line
            Log.d(TAG, "Complete message: " + completeMessage);

            // Process the message here
            String[] dataParts = completeMessage.split(";");
            if (dataParts.length >= 4) {
                try {
                    String mainCategory = dataParts[0].trim();
                    String subProduct = dataParts[1].trim();
                    float price = roundToTwoDecimals(Float.parseFloat(dataParts[2].trim()));
                    float weight = roundToTwoDecimals(Float.parseFloat(dataParts[3].trim()));

                    saveDataToDatabase(mainCategory, subProduct, price, weight);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing number: " + e.getMessage());
                }
            }
        }
    }


    private void saveDataToDatabase(String mainProduct, String subProduct, float productPrice, float productWeight) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        // Assume these methods return the correct IDs for the given product names
        String categoryId = lookupCategoryId(mainProduct);
        String subProductId = lookupSubProductId(subProduct);

        MyData product = new MyData();
        product.setMainProduct(mainProduct);
        product.setSubProduct(subProduct);
        product.setProductPrice(productPrice);
        product.setProductWeight(productWeight);
        product.setDate(date);

        product.setCategoryId(categoryId);
        product.setSubProductId(subProductId);

        product.saveToFirestore();
    }


    // Dummy methods for category and sub-product ID lookup
    private String lookupCategoryId(String mainProduct) {
        // Implement this method to return the category ID based on the main product name
        return "someCategoryId";
    }

    private String lookupSubProductId(String subProduct) {
        // Implement this method to return the sub-product ID based on the sub-product name
        return "someSubProductId";
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return null since this service is not designed for binding.
        return null;
    }
    private float roundToTwoDecimals(float value) {
        return Math.round(value * 100f) / 100f;
    }

}
