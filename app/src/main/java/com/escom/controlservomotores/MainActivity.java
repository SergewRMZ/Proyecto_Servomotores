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
import android.os.Handler;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private BluetoothService bluetoothService;
    private SeekBar servo1HorSeekBar, servo2HorSeekBar, servo1VerSeekBar, servo2VerSeekBar;
    private TextView servo1HorTextView, servo2HorTextView;
    private Button btnConnect;

    private int servo1HorAngle = 90;
    private int servo2HorAngle = 90;

    private Handler handler = new Handler();
    private Runnable sendRunnable;
    private static final int DELAY_MS = 100;

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
        servo1VerSeekBar = findViewById(R.id.servo1VerSeekBar);

        servo2VerSeekBar = findViewById(R.id.servo2VerSeekBar);
        servo1HorSeekBar.setMax(180);
        servo2HorSeekBar.setMax(180);
        servo1VerSeekBar.setMax(180);
        servo2VerSeekBar.setMax(180);

        servo1HorSeekBar.setProgress(servo1HorAngle);
        servo2HorSeekBar.setProgress(servo2HorAngle);
        servo1VerSeekBar.setProgress(servo1HorAngle);
        servo2VerSeekBar.setProgress(servo2HorAngle);

        servo1HorTextView.setText("Servo 1: " + servo1HorAngle);
        servo2HorTextView.setText("Servo 2: " + servo2HorAngle);

        servo1HorSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        servo2HorSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        servo1VerSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        servo2VerSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (id == R.id.servo1HorSeekBar || id == R.id.servo1VerSeekBar) {
                servo1HorAngle = progress;
                servo1HorTextView.setText("Servo 1: " + progress);

                // sincroniza ambas barras del servo 1 si el cambio fue del usuario
                if (fromUser) {
                    if (id == R.id.servo1HorSeekBar) {
                        servo1VerSeekBar.setProgress(progress);
                    } else {
                        servo1HorSeekBar.setProgress(progress);
                    }
                }

            } else if (id == R.id.servo2HorSeekBar || id == R.id.servo2VerSeekBar) {
                servo2HorAngle = progress;
                servo2HorTextView.setText("Servo 2: " + progress);

                // sincroniza ambas barras del servo 2 si el cambio fue del usuario
                if (fromUser) {
                    if (id == R.id.servo2HorSeekBar) {
                        servo2VerSeekBar.setProgress(progress);
                    } else {
                        servo2HorSeekBar.setProgress(progress);
                    }
                }
            }

            if (fromUser) {
                // Elimina tareas anteriores si el usuario sigue moviendo la barra
                if (sendRunnable != null) {
                    handler.removeCallbacks(sendRunnable);
                }

                // Nueva tarea de envío
                sendRunnable = new Runnable() {
                    @Override
                    public void run() {
                        sendAnglesToBluetooth();
                    }
                };

                // Envia datos después del retardo
                handler.postDelayed(sendRunnable, DELAY_MS);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
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
