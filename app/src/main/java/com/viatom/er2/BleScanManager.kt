package com.viatom.er2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class BleScanManager {
    interface Scan {
        fun scanReturn(name: String, bluetoothDevice: BluetoothDevice)
    }









    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var leScanner: BluetoothLeScanner
    private var scan: Scan? = null


    private fun parseRecord(scanRecord: ByteArray): Map<Int, String> {
        val ret: MutableMap<Int, String> = HashMap()
        var index = 0
        while (index < scanRecord.size) {
            val length = scanRecord[index++].toInt()
            //Zero value indicates that we are done with the record now
            if (length == 0) break
            val type = scanRecord[index].toInt()
            //if the type is zero, then we are pass the significant section of the data,
            // and we are thud done
            if (type == 0) break
            val data = Arrays.copyOfRange(scanRecord, index + 1, index + length)
            if (data.isNotEmpty()) {
                val hex = StringBuilder(data.size * 2)

                for (bb in data.indices) {
                    hex.append(String.format("%02X", data[bb]))
                }
                ret[type] = hex.toString()
            }
            index += length
        }
        return ret
    }
    private fun isRightScanRecord(bytes: ByteArray): Boolean {
        return "4EF301" == parseRecord(bytes)[-1]
    }


    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (device?.name == null) return;
            scan?.apply {
                if(device.name.contains("DuoEK")){
                    result.scanRecord?.let {
                        if(isRightScanRecord(it.bytes)){
                            scanReturn(device.name, device)
                        }
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {}
        override fun onScanFailed(errorCode: Int) {}
    }

    fun setCallBack(scan: Scan) {
        this.scan = scan
    }

    fun initScan(context: Context) {
        context.apply {
            val settings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()


            val bluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter
            leScanner = bluetoothAdapter!!.bluetoothLeScanner
            leScanner.startScan(null, settings, leScanCallback)
        }
    }

    fun stop() {
        leScanner.stopScan(leScanCallback)
    }
}