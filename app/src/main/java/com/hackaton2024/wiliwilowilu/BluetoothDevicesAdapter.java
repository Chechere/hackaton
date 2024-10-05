package com.hackaton2024.wiliwilowilu;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

@SuppressLint("MissingPermission")
public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.DeviceViewHolder> {
    private final Context context;
    private List<BluetoothDevice> devices;
    private Notifier notifier;

    public BluetoothDevicesAdapter(Context context, List<BluetoothDevice> devices, Notifier notifier) {
        this.devices = devices;
        this.context = context;
        this.notifier = notifier;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                    .inflate(android.R.layout.simple_list_item_1,
                                            parent,
                                            false);

        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.deviceInfo.setTextColor(Color.BLACK);
        holder.deviceInfo.setText(devices.get(position).getName());

        holder.itemView.setOnClickListener(v -> {
            notifier.notify(
                    context.getString(R.string.action_connect_bluetooth) + ": " + position);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceInfo;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceInfo = itemView.findViewById(android.R.id.text1);
        }
    }
}
