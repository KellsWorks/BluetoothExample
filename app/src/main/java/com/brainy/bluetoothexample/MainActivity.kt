package com.brainy.bluetoothexample

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.scanButton)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            // Handle this situation
        }

        // Register the BroadcastReceiver for device discovery
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(deviceDiscoveryReceiver, filter)

        button.setOnClickListener {
            checkBluetoothPermissions()
        }
    }

    private fun checkBluetoothPermissions() {
        val bluetoothPermission = Manifest.permission.BLUETOOTH
        val bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN

        val granted = PackageManager.PERMISSION_GRANTED
        val hasBluetoothPermission = ContextCompat.checkSelfPermission(this, bluetoothPermission) == granted
        val hasBluetoothAdminPermission = ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) == granted

        val permissions = ArrayList<String>()

        if (!hasBluetoothPermission) {
            permissions.add(bluetoothPermission)
        }
        if (!hasBluetoothAdminPermission) {
            permissions.add(bluetoothAdminPermission)
        }

        if (permissions.isNotEmpty()) {
            Log.d("checkBluetoothPermissions()", "Requesting permissions")
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_ENABLE_BT
            )
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                scanDevices()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scanDevices(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_ENABLE_BT)
        } else {
            bluetoothAdapter?.startDiscovery()
        }

    }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Handle the discovered device
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    Log.d("Bluetooth", "Found device: ${device?.name} - ${device?.address}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    // The discovery has started
                    Log.d("Bluetooth", "Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // The discovery has finished
                    Log.d("Bluetooth", "Discovery finished")
                }
            }
        }
    }


    private fun pairDevice(device: BluetoothDevice) {
        try {
            val pin = "1234"
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            device.setPin(pin.toByteArray())
            device.createBond()
        } catch (e: Exception) {
           Log.e("pairDevice()", e.message.toString())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object{
        private const val REQUEST_ENABLE_BT = 1
    }
}