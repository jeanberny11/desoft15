package com.desoft.desoft15.utils;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.desoft.desoft15.R;

import java.util.List;

public class BluetoothDevicesListAdapter extends BaseAdapter {
    private final Context context;
    private final List<BluetoothDevice> devices;

    public BluetoothDevicesListAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final BluetoothDevice item = devices.get(i);
        if (view == null) {
            view = LayoutInflater.from(context).
                    inflate(R.layout.bluetoothdevice_item, viewGroup, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                ((TextView) view.findViewById(R.id.txtnombre))
                        .setText(item.getName());
                ((TextView) view.findViewById(R.id.txtaddress))
                        .setText(item.getAddress());
            }else {
                ((TextView) view.findViewById(R.id.txtnombre))
                        .setText("No disponible");
                ((TextView) view.findViewById(R.id.txtaddress))
                        .setText("No disponible");
            }
        }else{
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                ((TextView) view.findViewById(R.id.txtnombre))
                        .setText(item.getName());
                ((TextView) view.findViewById(R.id.txtaddress))
                        .setText(item.getAddress());
            }else {
                ((TextView) view.findViewById(R.id.txtnombre))
                        .setText("No disponible");
                ((TextView) view.findViewById(R.id.txtaddress))
                        .setText("No disponible");
            }
        }
        return view;
    }
}
