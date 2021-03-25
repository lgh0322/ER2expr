package com.viatom.er2.activity

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.viatom.er2.blepower.BleDataManager
import com.viatom.er2.blepower.BleDataWorker
import com.viatom.er2.blepower.BleScanManager
import com.viatom.er2.R
import com.viatom.er2.blething.BleCmd.getRtData
import com.viatom.er2.blething.Gua
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), BleScanManager.Scan {
    val dataScope = CoroutineScope(Dispatchers.IO)
    private val scan = BleScanManager()
    private lateinit var myBleDataManager: BleDataManager
    private val bleDataWorker: BleDataWorker = BleDataWorker()
    lateinit var er2:BluetoothDevice
    lateinit var pr:BluetoothDevice
    var er2Connect=false
    var prConnect=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Gua.initVar(this)
        initScan()


        myBleDataManager = BleDataManager(this)
    }
    private fun initScan() {
        scan.initScan(this)
        scan.setCallBack(this)
    }

    override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
        if(name.contains("DuoEK")){
            scan.stop()
            if(!er2Connect){
                er2Connect=true
                er2=bluetoothDevice
                bleDataWorker.initWorker(this@MainActivity,bluetoothDevice)
                dataScope.launch {
                    bleDataWorker.waitConnect()
                    Timer().schedule(getPinTimer, Date(),500)
                }

            }
        }

    }

    fun View.getFileList() {
        dataScope.launch {
            bleDataWorker.getFileList()
        }

    }

    fun View.fileSize() {
        dataScope.launch {
            bleDataWorker.getFile(BleDataWorker.gua.fileList[2])
        }
    }

    inner class PinTimerTask() : TimerTask() {
        override fun run() {
            dataScope.launch {
                val x=bleDataWorker.getData()
                Log.e("ga","${x.wave.len}")
            }
        }
    }

    var getPinTimer=PinTimerTask()

    fun getFi(view: View) {

        dataScope.launch {
            val x=bleDataWorker.getData()
            Log.e("ga","${x.wave.len}")
        }
    }
}
