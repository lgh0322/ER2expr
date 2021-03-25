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
import com.viatom.er2.view.WaveView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), BleScanManager.Scan {
    val dataScope = CoroutineScope(Dispatchers.IO)
    private val scan = BleScanManager()
    private lateinit var myBleDataManager: BleDataManager
    lateinit var waveView: WaveView
    private val bleDataWorker: BleDataWorker = BleDataWorker()
    lateinit var er2:BluetoothDevice
    lateinit var pr:BluetoothDevice
    var er2Connect=false
    var prConnect=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waveView=findViewById(R.id.wave)
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

    var begina=0
    inner class PinTimerTask() : TimerTask() {
        override fun run() {
            dataScope.launch {
                val x=bleDataWorker.getData()
                 x.wave.wFs?.let {
                     for(k in it){
                         da.add(k)
                     }
                }

                if(da.size>200){
                    if(begina==0){
                        begina=1
                        Timer().schedule(dr, Date(),32)
                    }
                }

            }
        }
    }



    fun add(ori: FloatArray?, add:FloatArray): FloatArray {
        if (ori == null) {
            return add
        }

        val new: FloatArray = FloatArray(ori.size + add.size)
        for ((index, value) in ori.withIndex()) {
            new[index] = value
        }

        for ((index, value) in add.withIndex()) {
            new[index + ori.size] = value
        }

        return new
    }




    var da:ArrayList<Float> = ArrayList()
    var currentIndex=0
    val dr=DrawTask()
    inner class DrawTask() : TimerTask() {
        override fun run() {
            MainScope().launch {
                for(k in 0 until 4){

                    if(da.isNotEmpty()){
                        waveView.data[currentIndex]= (da[0]*200).toInt()
                        da.removeAt(0)
                    }else{
                        break
                    }
                    currentIndex++
                    if(currentIndex>=500){
                        currentIndex-=500
                    }

                }
                waveView.invalidate()
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
