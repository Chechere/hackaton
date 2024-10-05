package com.hackaton2024.wiliwilowilu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity implements Notifier {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevicesAdapter bluetoothDevicesAdapter;
    private List<BluetoothDevice> devicesList;
    private List<BluetoothDevice> filteredList;

    private boolean bluetoothActivated = false;
    private boolean connectingDevice = false;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        @SuppressLint("MissingPermission")
        //The permissions have already been checked and processed previously
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getAction() == null) {
                return;
            }

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if ((device != null && !devicesList.contains(device)) &&
                        (device.getName() != null && isValidDevice(device.getName()))) {
                    devicesList.add(device);
                    filteredList.add(device);

                    bluetoothDevicesAdapter.notifyItemInserted(devicesList.size() - 1);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(!isBluetoothCheckedAndActivated()) {
            Toast.makeText(this,
                    getString(R.string.error_checking_active_bluetooth),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        EditText editText = findViewById(R.id.BluetoothEditTextFilter);
        RecyclerView bluetoothList = findViewById(R.id.ListaBluetooth);

        bluetoothList.setLayoutManager(new LinearLayoutManager(this));

        devicesList = new ArrayList<>();
        filteredList = new ArrayList<>();

        bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this, filteredList, this);
        bluetoothList.setAdapter(bluetoothDevicesAdapter);

        getBondedDevices();
        startGetNewDevices();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterDevices(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    boolean isBluetoothCheckedAndActivated() {
        return checkBluetoothPermissions() && activateBluetooth();
    }

    boolean checkBluetoothPermissions() {
        String permisosFaltantes = "";
        boolean tienePermisos = true;

        boolean faltanBluetoothPermAPI31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED);

        boolean faltanBluetoothPermResto = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED;

        boolean faltanLocationPerm = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        if (faltanBluetoothPermAPI31 || faltanBluetoothPermResto) {
            permisosFaltantes = "Acceso al Bluetooth";
            tienePermisos = false;
        }

        if (faltanLocationPerm) {
            if(!permisosFaltantes.isEmpty()) {
                permisosFaltantes += ", ";
            }

            permisosFaltantes += "Acceso a la ubicación";
            tienePermisos = false;
        }

        if(!tienePermisos) {
            new AlertDialog.Builder(this)
                .setTitle("No tengo los suficientes permisos:")
                .setMessage("Esta aplicación necesita los siguientes permisos para funcionar: " +
                            permisosFaltantes + ".\nPor favor, otorga los permisos.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);

                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
        }

        return tienePermisos;
    }

    boolean activateBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Este dispositivo no soporta la tecnología bluetooth",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() != RESULT_OK) {
                            Toast.makeText(this,
                                    "Se necesita el bluetooth activado para funcionar",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            bluetoothActivated = true;
                        }
                    }
            );

            enableBluetoothLauncher.launch(enableBtIntent);
        } else {
            bluetoothActivated = true;
        }

        return bluetoothActivated;
    }

    @SuppressLint("MissingPermission")
    //The permissions have already been checked and processed previously
    private void getBondedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            for (BluetoothDevice bd: pairedDevices) {
                if(bd != null && bd.getName() != null && isValidDevice(bd.getName())) {
                    devicesList.add(bd);
                }
            }
        }

        filteredList.addAll(devicesList);
        bluetoothDevicesAdapter.notifyItemRangeInserted(0, devicesList.size());
    }

    @SuppressLint("MissingPermission")
    //The permissions have already been checked and processed previously
    private void startGetNewDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    private boolean isValidDevice(String name) {
        return name.startsWith(getString(R.string.prefix_bt_device_name));
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }

    @SuppressLint("MissingPermission")
    //The permissions have already been checked and processed previously
    private void filterDevices(String text) {
        int oldNumberDevices = filteredList.size();

        filteredList.clear();
        bluetoothDevicesAdapter.notifyItemRangeRemoved(0, oldNumberDevices);

        for (BluetoothDevice device : devicesList) {
            if((device != null && device.getName() != null) &&
                    device.getName().toLowerCase().contains(text)) {
                filteredList.add(device);
            }
        }

        bluetoothDevicesAdapter.notifyItemRangeInserted(0, filteredList.size());
    }

    @Override
    @SuppressLint("MissingPermission")
    //The permissions have already been checked and processed previously
    public void notify(String notification) {
        if(connectingDevice ||
                !notification.startsWith(getString(R.string.action_connect_bluetooth))) {
            return;
        }

        connectingDevice = true;

        int pos = Integer.parseInt(
                        notification.substring(getString(R.string.action_connect_bluetooth).length() + 2, notification.length())
                        .trim());

        BluetoothDeviceManager.getInstance().setBluetoothDevice(filteredList.get(pos));

        setResult(Activity.RESULT_OK);
        finish();
    }
}