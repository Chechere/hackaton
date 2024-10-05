package com.hackaton2024.wiliwilowilu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

public class BluetoothDataHandler implements Runnable {
    private Dictionary<String, MyButton> buttons;
    private BluetoothDevice device;
    final private Activity ACTIVITY;
    final private Notifier NOTIFIER;
    private boolean running = false;


    public BluetoothDataHandler(Activity activity, Notifier notifier) {
        buttons = new Hashtable<>();

        this.device = null;
        this.ACTIVITY = activity;
        this.NOTIFIER = notifier;
    }

    public boolean addButton(String id, MyButton tv) {
        if (id == null || tv == null) {
            return false;
        }

        buttons.put(id, tv);

        return true;
    }

    public void setDevice(BluetoothDevice device) {
        System.out.println(device);
        if(device == null) {
            return;
        }

        this.device = device;
    }

    public void close() {
        this.running = false;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void run() {
        if(this.device == null) {
            return;
        }

        this.running = true;

        UUID uuid = UUID.fromString(this.ACTIVITY.getString(R.string.bluetooth_serial_uid));

        try (BluetoothSocket bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)) {
            bluetoothSocket.connect();

            if(bluetoothSocket.isConnected()) {
                System.out.println("ENTRA");

                byte[] data = new byte[1024];

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.setStrictness(Strictness.LENIENT).create();

                InputStream is = bluetoothSocket.getInputStream();
                String json = "";

                while(this.running && bluetoothSocket.isConnected()) {
                    System.out.println("LEYENDO");
                    int lenght = is.read(data);

                    System.out.println("LEIDO");
                    String data_str = new String(data, 0, lenght);

                    json += data_str.trim();
                    System.out.println(json);

                    if(json.endsWith("}")) {
                        JsonDataPack dataPack = gson.fromJson(json, JsonDataPack.class);

                        this.ACTIVITY.runOnUiThread(() -> {
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_1_name))
                                    .setValue(dataPack.getPressure());
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_2_name))
                                    .setValue(dataPack.getTemperature());
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_3_name))
                                    .setValue(dataPack.getAirHumidity());
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_4_name))
                                    .setValue(dataPack.getSoilHumidity());
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_5_name))
                                    .setValue(dataPack.getLuminosity());
                            buttons.get(this.ACTIVITY.getString(R.string.sensor_6_name))
                                    .setValue(dataPack.getpH());

                        });

                        json = "";
                    }
                }

                this.ACTIVITY.runOnUiThread(() ->
                {
                    Toast.makeText(this.ACTIVITY,
                            "Connection with the device has been closed",
                            Toast.LENGTH_SHORT).show();
                });

                this.NOTIFIER.notify("CONNECTION CLOSED");
            }
        } catch (IOException e) {
            e.printStackTrace();

            this.ACTIVITY.runOnUiThread(() ->
            {
                Toast.makeText(this.ACTIVITY,
                        "Error with bluetooth connection",
                        Toast.LENGTH_SHORT).show();
            });

            this.NOTIFIER.notify("CONNECTION CLOSED");
        }

        this.running = false;
    }
}
