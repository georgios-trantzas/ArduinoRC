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

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BluetoothArrayAdapter extends ArrayAdapter<CustomBluetoothDevice> {

    private Context mContext;
    private List<CustomBluetoothDevice> mDevices;

    public BluetoothArrayAdapter(Context context, ArrayList<CustomBluetoothDevice> devices) {

        super(context, 0, devices);
        mContext = context;
        mDevices = devices;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, parent, false);

        CustomBluetoothDevice currentDevice = mDevices.get(position);

        ImageView image = listItem.findViewById(R.id.device_list_item_icon);

        if (currentDevice.getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {

            image.setImageResource(R.drawable.phone_icon);

        } else if (currentDevice.getDeviceClass() == BluetoothClass.Device.COMPUTER_DESKTOP) {

            image.setImageResource(R.drawable.desktop_icon);

        } else if (currentDevice.getDeviceClass() == BluetoothClass.Device.COMPUTER_LAPTOP) {

            image.setImageResource(R.drawable.laptop_icon);

        } else {

            image.setImageResource(R.drawable.bluetooth_icon);
        }


        TextView name = listItem.findViewById(R.id.device_list_item_name);
        name.setText(currentDevice.getDeviceName());

        TextView address = listItem.findViewById(R.id.device_list_item_address);
        address.setText(currentDevice.getDeviceAddress());

        return listItem;

    }
}
