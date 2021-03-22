package com.viatom.er2

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity(), BleScanManager.Scan {
    private val scan = BleScanManager()
    private lateinit var myBleDataManager: BleDataManager
    lateinit var er2:BluetoothDevice
    lateinit var pr:BluetoothDevice
    var er2Connect=false
    var prConnect=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScan()


        myBleDataManager = BleDataManager(this)
    }
    private fun initScan() {
        scan.initScan(this)
        scan.setCallBack(this)
    }

    override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
        if(name.contains("DuoEK")){
            if(!er2Connect){
                er2Connect=true
                er2=bluetoothDevice
                er2.let {
                    myBleDataManager.connect(it)
                            .useAutoConnect(true)
                            .timeout(10000)
                            .retry(15, 100)
                            .done {
                                Log.i("BLE", "连接成功了.>>.....>>>>")
                            }
                            .enqueue()
                }

            }
        }

    }
}