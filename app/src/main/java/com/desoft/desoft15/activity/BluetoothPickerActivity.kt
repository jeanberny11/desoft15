package com.desoft.desoft15.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.desoft.desoft15.R
import com.desoft.desoft15.databinding.ActivityBluetoothPickerBinding
import com.desoft.desoft15.utils.BluetoothDevicesListAdapter

class BluetoothPickerActivity : AppCompatActivity() {

    lateinit var listAdapter: BluetoothDevicesListAdapter
    private lateinit var binding: ActivityBluetoothPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothPickerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_CONNECT"
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_SCAN"
                ) == PackageManager.PERMISSION_GRANTED
            ){
                listAdapter= BluetoothDevicesListAdapter(this,getBluetoothDevicesList(this))
                binding.content.bluetoothdeviceslistview.adapter=listAdapter
            }else{
                val permissions = ArrayList<String>()
                permissions.add("android.permission.BLUETOOTH_CONNECT")
                permissions.add("android.permission.BLUETOOTH_SCAN")
                val perm = arrayOfNulls<String>(permissions.size)
                permissions.toArray(perm)
                ActivityCompat.requestPermissions(this, perm, 20)
            }
        }else{
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.BLUETOOTH_ADMIN"
                ) == PackageManager.PERMISSION_GRANTED
            ){
                listAdapter= BluetoothDevicesListAdapter(this,getBluetoothDevicesListLegacy(this))
                binding.content.bluetoothdeviceslistview.adapter=listAdapter
            }else{
                val permissions = ArrayList<String>()
                permissions.add("android.permission.BLUETOOTH_ADMIN")
                val perm = arrayOfNulls<String>(permissions.size)
                permissions.toArray(perm)
                ActivityCompat.requestPermissions(this, perm, 20)
            }
        }
        binding.content.bluetoothdeviceslistview.setOnItemClickListener { _, _, i, _ ->
            val item=listAdapter.getItem(i) as BluetoothDevice
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        "android.permission.BLUETOOTH_CONNECT"
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    intent.putExtra("name",item.name)
                    intent.putExtra("address",item.address)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                } else {
                    Toast.makeText(this@BluetoothPickerActivity,"La app no cuenta con los permisos requeridos para obtener los dispositivos vinculados",Toast.LENGTH_LONG).show();
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        "android.permission.BLUETOOTH_ADMIN"
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    intent.putExtra("name",item.name)
                    intent.putExtra("address",item.address)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                } else {
                    Toast.makeText(this@BluetoothPickerActivity,"La app no cuenta con los permisos requeridos para obtener los dispositivos vinculados",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"])
    fun getBluetoothDevicesList(context: Context): List<BluetoothDevice> {
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if (!adapter.isEnabled) {
            throw Exception("El bluetooth del dispositivo no esta habilitado")
        }
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
        return adapter.bondedDevices.toList()
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    fun getBluetoothDevicesListLegacy(context: Context): List<BluetoothDevice> {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            throw Exception("Funcion no compatible con esta version de android")
        }
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("Funcion no compatible con esta version de android")
        }
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if (!adapter.isEnabled) {
            throw Exception("El bluetooth del dispositivo no esta habilitado")
        }
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
        return adapter.bondedDevices.toList()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            20 -> {
                finish()
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}