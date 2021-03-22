package com.viatom.er2

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity(), BleScanManager.Scan {
    val scan = BleScanManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScan()
    }
    private fun initScan() {
        scan.initScan(this)
        scan.setCallBack(this)
    }

    override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
        Log.e("fuck",name)
    }
}