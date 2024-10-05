package com.hackaton2024.wiliwilowilu;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements Notifier {
    ActivityResultLauncher<Intent> bluetoothActivityLauncher;

    TextView deviceState;
    BluetoothDataHandler bluetoothDataHandler;

    boolean isConnected = true;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceState = findViewById(R.id.DeviceState);

//        MyButton pressureMeasure = findViewById(R.id.sensor1);
//        MyButton tempMeasure = findViewById(R.id.sensor2);
//        MyButton humAirMeasure = findViewById(R.id.sensor3);
//        MyButton humSoilMeasure = findViewById(R.id.sensor4);
//        MyButton luminosityMeasure = findViewById(R.id.sensor5);
//        MyButton pHMeasure = findViewById(R.id.sensor6);

        bluetoothDataHandler = new BluetoothDataHandler(this, this);
        bluetoothDataHandler.addButton(getString(R.string.sensor_1_name), findViewById(R.id.sensor1));
        bluetoothDataHandler.addButton(getString(R.string.sensor_2_name), findViewById(R.id.sensor2));
        bluetoothDataHandler.addButton(getString(R.string.sensor_3_name), findViewById(R.id.sensor3));
        bluetoothDataHandler.addButton(getString(R.string.sensor_4_name), findViewById(R.id.sensor4));
        bluetoothDataHandler.addButton(getString(R.string.sensor_5_name), findViewById(R.id.sensor5));
        bluetoothDataHandler.addButton(getString(R.string.sensor_6_name), findViewById(R.id.sensor6));

        ImageButton bluetoothButton = findViewById(R.id.ButtonBluetooth);
        ImageButton NASAButton = findViewById(R.id.ButtonNASA);

        bluetoothButton.setOnClickListener(this::onBluetoothClick);
        NASAButton.setOnClickListener(this::onNASAClick);

        getPermissions();

        bluetoothActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        //isConnected = true;

                        BluetoothDevice device = BluetoothDeviceManager.getInstance().getBluetoothDevice();

                        deviceState.setText(getString(R.string.bluetooth_connected_text));
                        deviceState.setBackgroundColor(getColor(R.color.verde2));

                        bluetoothDataHandler.setDevice(device);
                        new Thread(bluetoothDataHandler).start();

                        this.isConnected = true;
                    }
                }
        );
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.
                    permission.BLUETOOTH_CONNECT}, 1);
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
    }

    void onBluetoothClick(View v) {
        if(this.isConnected) {
            bluetoothDataHandler.close();
        }

        Intent i = new Intent(this, BluetoothActivity.class);
        bluetoothActivityLauncher.launch(i);
    }

    void onNASAClick(View v) {
        Intent i = new Intent(this, RegionInfoActivity.class);

        startActivity(i);
    }

    @Override
    public void notify(String notification) {
        if(notification.equals("CONNECTION CLOSED")) {
            deviceState.setText(getString(R.string.bluetooth_disconnected_text));
            deviceState.setBackgroundColor(getColor(R.color.rojo));

            this.isConnected = false;
        }
    }
}