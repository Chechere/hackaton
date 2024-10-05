package com.hackaton2024.wiliwilowilu;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothDeviceManager {
    private static BluetoothDeviceManager instance;
    private BluetoothDevice bluetoothDevice;

    private BluetoothDeviceManager() {}

    public static BluetoothDeviceManager getInstance() {
        if (instance == null) {
            instance = new BluetoothDeviceManager();
        }
        return instance;
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        this.bluetoothDevice = device;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
}
