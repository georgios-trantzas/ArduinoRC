/*
 * Copyright 2019 Georgios Trantzas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.giwrgostrantzas.arduinorc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothArrayAdapter bluetoothArrayAdapter;
    public static ConnectedThread ct = null;
    public boolean disconnectedOnPurpose;

    // Create broadcastReceiver to check if connection is lost
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                // Check if connection loss in the ControllerActivity happened on purpose
                if (disconnectedOnPurpose){

                    Toast.makeText(context, getResources().getString(R.string.rc_disconnected_toast), Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(i);

                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the disconnectOnPurpose value from the intent
        Intent intent = getIntent();
        disconnectedOnPurpose = intent.getBooleanExtra("disconnectOnPurpose", false);

        int REQUEST_ENABLE_BT = 1;

        Button pairedButton = findViewById(R.id.main_activity_paired_devices_button);
        Button about = findViewById(R.id.main_activity_about_button);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if device supports bluetooth
        if (bluetoothAdapter == null) {

            Toast.makeText(this,  getResources().getString(R.string.no_bluetooth_support_toast), Toast.LENGTH_LONG).show();

        }else{

            // Enable bluetooth if it is disabled
            if (!bluetoothAdapter.isEnabled()) {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            pairedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listPairedDevices();

                }
            });
        }

        // About button sends an intent to AboutActivity
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    // This method creates an alert dialog with paired devices list
    public void listPairedDevices() {

        Set<BluetoothDevice> pairedDevices;
        pairedDevices = bluetoothAdapter.getBondedDevices();

        ArrayList<CustomBluetoothDevice> customBluetoothDevices = new ArrayList<>();

        for (BluetoothDevice device : pairedDevices){

            customBluetoothDevices.add(new CustomBluetoothDevice(device.getName(), device.getAddress(), device.getBluetoothClass().getDeviceClass()));

        }

        bluetoothArrayAdapter = new BluetoothArrayAdapter(this, customBluetoothDevices);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.main_activity_paired_devices_dialog_title)).setAdapter(bluetoothArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                BluetoothDevice currentDevice = bluetoothAdapter.getRemoteDevice(bluetoothArrayAdapter.getItem(which).getDeviceAddress());
                ConnectThread connectThread = new ConnectThread(currentDevice);
                connectThread.run();

            }
        });

        Dialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {

            unregisterReceiver(receiver);

        }catch (IllegalArgumentException e){

            Log.v("MainActivity: ", e.toString());
        }
    }


    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;
            mmDevice = device;

            try {

                // Get a BluetoothSocket to connect with the given BluetoothDevice
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {

                Log.v("MainActivity: ", e.toString());
            }

            mmSocket = tmp;
        }

        public void run() {

            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {

                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            } catch (IOException connectException) {


                try {

                    // Unable to connect; close the socket and get out
                    mmSocket.close();
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.connection_failed_toast), Toast.LENGTH_SHORT).show();

                } catch (IOException closeException) {

                    Log.v("MainActivity: ", closeException.toString());
                }

                return;
            }

            // Do work to manage the connection
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(final BluetoothSocket mmSocket) {

            Toast.makeText(MainActivity.this, getResources().getString(R.string.device_is_connected_toast), Toast.LENGTH_SHORT).show();
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

            ct = new ConnectedThread(mmSocket);

            Intent i = new Intent(MainActivity.this, ControllerActivity.class);
            startActivity(i);
        }

    }

    public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            OutputStream tmpOut = null;

            try {

                tmpOut = socket.getOutputStream();

            } catch (IOException e) {

                Log.v("MainActivity: ", e.toString());
            }

            mmOutStream = tmpOut;
        }

        // Call this from the ControllerActivity to send data to the remote device
        public void write(byte[] bytes) {

            try {

                mmOutStream.write(bytes);

            } catch (IOException e) {

                Log.v("MainActivity: ", e.toString());
            }
        }

        // Call this from the ControllerActivity to shutdown the connection
        public void cancel() {

            try {

                mmSocket.close();

            } catch (IOException e) {

                Log.v("MainActivity: ", e.toString());
            }
        }

    }
}
