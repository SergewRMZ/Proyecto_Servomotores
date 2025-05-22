package com.escom.controlservomotores;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private BluetoothService bluetoothService;
    private SeekBar servo1HorSeekBar, servo2HorSeekBar;
    private TextView servo1HorTextView, servo2HorTextView;
    private Button btnConnect;

    private int servo1HorAngle = 90;
    private int servo2HorAngle = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothService = new BluetoothService(this);

        // Se solicitan permisos para utilizar bluetooth al usuario.
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(v -> {
            if(!bluetoothService.hasBluetoothPermissions()) {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.BLUETOOTH_CONNECT }, 2);

                if(!bluetoothService.bluetoothAdapterEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivityForResult(intent, 1);
                }   else {
                    Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();
                    bluetoothService.connect();
                }
            }
        });

        servo1HorSeekBar = findViewById(R.id.servo1HorSeekBar);
        servo2HorSeekBar = findViewById(R.id.servo2HorSeekBar);

        servo1HorTextView = findViewById(R.id.servo1HorTextView);
        servo2HorTextView = findViewById(R.id.servo2HorTextView);

        servo1HorSeekBar.setMax(180);
        servo2HorSeekBar.setMax(180);

        servo1HorSeekBar.setProgress(servo1HorAngle);
        servo2HorSeekBar.setProgress(servo2HorAngle);

        servo1HorTextView.setText("Servo 1: " + servo1HorAngle);
        servo2HorTextView.setText("Servo 2: " + servo2HorAngle);

        servo1HorSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        servo2HorSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (id == R.id.servo1HorSeekBar) {
                servo1HorAngle = progress;
                servo1HorTextView.setText("Servo 1: " + progress);
            }   else if (id == R.id.servo2HorSeekBar) {
                servo2HorAngle = progress;
                servo2HorTextView.setText("Servo 2: " + progress);
            }
            sendAnglesToBluetooth();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private void sendAnglesToBluetooth() {
        String data = servo1HorAngle + "," + servo2HorAngle + "\n";
        bluetoothService.sendData(data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.close();
    }
}
