package com.example.javanavigationbar;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service started");
        bluetoothSocket = BluetoothSocketSingleton.getInstance().getBluetoothSocket();

        if (bluetoothSocket != null) {
            try {
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
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

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Received: " + readMessage);

                    processData(readMessage);  // Process the data here

                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }).start();
    }

    private StringBuilder dataBuffer = new StringBuilder();

    private void processData(String data) {
        Log.d(TAG, "Processing data: " + data);

        // Append the incoming data to the buffer
        dataBuffer.append(data);

        // Check if the buffer contains a complete message
        String[] dataParts = dataBuffer.toString().split(";");
        if (dataParts.length >= 4) {
            // Extract the complete message from the buffer
            String completeMessage = dataBuffer.toString();

            // Clear the buffer
            dataBuffer.setLength(0);

            // Now you can process the complete message
            Log.d(TAG, "dataParts length: " + dataParts.length); // log the length of the array
            Log.d(TAG, "Complete message: " + completeMessage);
            for(String part : dataParts) { // log each part
                Log.d(TAG, "Part: " + part);
            }

            int storageNumber = Integer.parseInt(dataParts[0].trim());
            String name = dataParts[1].trim();
            int price = Integer.parseInt(dataParts[2].trim());
            int weight = Integer.parseInt(dataParts[3].trim());

            saveDataToDatabase(storageNumber, name, price, weight);
        }
    }




    private void saveDataToDatabase(int storageNumber, String productName,
                                    int productPrice, int productWeight) {

        MyData.ProductType productType;
        try {
            productType = MyData.ProductType.valueOf(productName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unknown product name: " + productName);
            return;
        }

        // Get current date
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        MyData product = new MyData(storageNumber, productType, productPrice, productWeight, date);

        // Save the data to Firestore using the saveToFirestore() method of MyData
        product.saveToFirestore();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed to be bound, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the input stream", e);
        }
    }
}
