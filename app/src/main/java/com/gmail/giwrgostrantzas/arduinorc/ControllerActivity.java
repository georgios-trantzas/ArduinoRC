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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ControllerActivity extends AppCompatActivity {


    String forwardKey;
    String reverseKey;
    String leftKey;
    String rightKey;
    String stopKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller);

        // Get the saved preferences for the bluetooth keys
        final SharedPreferences preferences = getApplicationContext().getSharedPreferences("keys", 0);
        forwardKey = preferences.getString("forward", "1");
        reverseKey = preferences.getString("reverse", "2");
        leftKey = preferences.getString("left", "3");
        rightKey = preferences.getString("right", "4");
        stopKey = preferences.getString("stop", "5");


        final ImageView forward = findViewById(R.id.controller_activity_forward_button);
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        forward();
                        forward.setImageResource(R.drawable.arrow_icon_gray);

                        return true;

                    case MotionEvent.ACTION_UP:

                        stop();
                        forward.setImageResource(R.drawable.arrow_icon_white);

                        return true;
                }
                return false;
            }


        });

        final ImageView reverse = findViewById(R.id.controller_activity_reverse_button);
        reverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        reverse();
                        reverse.setImageResource(R.drawable.arrow_icon_gray);

                        return true;

                    case MotionEvent.ACTION_UP:

                        stop();
                        reverse.setImageResource(R.drawable.arrow_icon_white);

                        return true;
                }
                return false;
            }
        });

        final ImageView left = findViewById(R.id.controller_activity_left_button);
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        left();
                        left.setImageResource(R.drawable.arrow_icon_gray);

                        return true;

                    case MotionEvent.ACTION_UP:

                        stop();
                        left.setImageResource(R.drawable.arrow_icon_white);

                        return true;
                }
                return false;
            }
        });

        final ImageView right = findViewById(R.id.controller_activity_right_button);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        right();
                        right.setImageResource(R.drawable.arrow_icon_gray);

                        return true;

                    case MotionEvent.ACTION_UP:

                        stop();
                        right.setImageResource(R.drawable.arrow_icon_white);

                        return true;
                }
                return false;
            }
        });

        Button back = findViewById(R.id.controller_activity_back_button);

        // Back button sends an intent to the MainActivity and notify that connection loss happened on purpose.
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.ct.cancel();

                Toast.makeText(ControllerActivity.this, getResources().getString(R.string.rc_disconnected_toast), Toast.LENGTH_SHORT).show();

                Intent i = new Intent(ControllerActivity.this, MainActivity.class);
                i.putExtra("disconnectOnPurpose", true);
                startActivity(i);
            }
        });

        // Keys button creates a dialog with bluetooth keys settings
        final Button keys = findViewById(R.id.controller_activity_keys_button);
        keys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(ControllerActivity.this);

                dialog.setContentView(R.layout.keys_dialog);

                final EditText forwardKeyEditText = dialog.findViewById(R.id.keys_dialog_forward_key_edit_text);
                forwardKeyEditText.setText(forwardKey);

                final EditText reverseKeyEditText = dialog.findViewById(R.id.keys_dialog_reverse_key_edit_text);
                reverseKeyEditText.setText(reverseKey);

                final EditText leftKeyEditText = dialog.findViewById(R.id.keys_dialog_left_key_edit_text);
                leftKeyEditText.setText(leftKey);

                final EditText rightKeyEditText = dialog.findViewById(R.id.keys_dialog_right_key_edit_text);
                rightKeyEditText.setText(rightKey);

                final EditText stopKeyEditText = dialog.findViewById(R.id.keys_dialog_brake_key_edit_text);
                stopKeyEditText.setText(stopKey);


                Button saveButton = dialog.findViewById(R.id.keys_dialog_save_button);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Check if there are stored bluetooth keys and we restore them
                        SharedPreferences.Editor keysEditor = preferences.edit();

                        if (forwardKeyEditText.getText() != null) {

                            forwardKey = forwardKeyEditText.getText().toString();
                            keysEditor.putString("forward", forwardKey);


                        }

                        if (reverseKeyEditText.getText() != null) {

                            reverseKey = reverseKeyEditText.getText().toString();
                            keysEditor.putString("reverse", reverseKey);
                        }

                        if (leftKeyEditText.getText() != null) {

                            leftKey = leftKeyEditText.getText().toString();
                            keysEditor.putString("left", leftKey);
                        }

                        if (rightKeyEditText.getText() != null) {

                            rightKey = rightKeyEditText.getText().toString();
                            keysEditor.putString("right", rightKey);
                        }

                        if (stopKeyEditText.getText() != null) {

                            stopKey = stopKeyEditText.getText().toString();
                            keysEditor.putString("stop", stopKey);
                        }

                        keysEditor.apply();
                        dialog.dismiss();

                    }
                });

                dialog.show();
            }
        });
    }

    private void forward() {
        MainActivity.ct.write(forwardKey.getBytes());
    }

    private void reverse() {
        MainActivity.ct.write(reverseKey.getBytes());
    }

    private void left() {
        MainActivity.ct.write(leftKey.getBytes());
    }

    private void right() {
        MainActivity.ct.write(rightKey.getBytes());
    }

    private void stop() {
        MainActivity.ct.write(stopKey.getBytes());
    }

}
