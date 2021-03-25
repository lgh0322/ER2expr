package com.viatom.er2.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.viatom.er2.R
import com.viatom.er2.blepower.BleDataManager
import com.viatom.er2.blepower.BleDataWorker
import com.viatom.er2.blepower.BleScanManager
import com.viatom.er2.blething.PathUtil
import com.viatom.er2.view.WaveView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    lateinit var er2: BluetoothDevice
    var er2Connect = false
    var ecgBuffer: ArrayList<Float> = ArrayList()
    var currentUpdateIndex = 0
    val drawTask = DrawTask()
    var beginDraw = false
    var rtDataTask = RtDataTask()

    companion object {
        external fun filter(f: Double, reset: Boolean): DoubleArray?
        external fun shortFilter(shorts: ShortArray?): ShortArray?
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("offline-lib");
            System.loadLibrary("native-lib");
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waveView = findViewById(R.id.wave)
        PathUtil.initVar(this)
        initScan()


        myBleDataManager = BleDataManager(this)
    }

    private fun initScan() {
        scan.initScan(this)
        scan.setCallBack(this)
    }

    override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
        if (name.contains("DuoEK")) {
            scan.stop()
            if (!er2Connect) {
                er2Connect = true
                er2 = bluetoothDevice
                bleDataWorker.initWorker(this@MainActivity, bluetoothDevice)
                dataScope.launch {
                    bleDataWorker.waitConnect()
                    Timer().schedule(rtDataTask, Date(), 500)
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



    inner class RtDataTask() : TimerTask() {
        override fun run() {
            dataScope.launch {
                val x = bleDataWorker.getData()
                x.wave.wFs?.let {
                    for (k in it) {
                        val doubleArray: DoubleArray? = filter(k.toDouble(), reset = false)
                        doubleArray?.run {
                            if (doubleArray.isNotEmpty()) {
                                for (j in doubleArray) {
                                    ecgBuffer.add(j.toFloat())
                                }

                            }
                        }
                    }
                }

                if (ecgBuffer.size > 200) {
                    if (!beginDraw) {
                        beginDraw = true
                        Timer().schedule(drawTask, Date(), 32)
                    }
                }

            }
        }
    }








    inner class DrawTask() : TimerTask() {
        override fun run() {

            for (k in 0 until 4) {
                if (ecgBuffer.isNotEmpty()) {
                    waveView.data[currentUpdateIndex] = (ecgBuffer[0] * 200).toInt()
                    ecgBuffer.removeAt(0)
                } else {
                    break
                }
                currentUpdateIndex++
                if (currentUpdateIndex >= 500) {
                    currentUpdateIndex -= 500
                }

            }
            var cc=currentUpdateIndex

            for (k in 0 until 5) {
                waveView.data[cc] = 0.toInt()
                cc++
                if (cc >= 500) {
                    cc-= 500
                }

            }
            waveView.invalidate()


        }
    }




    fun getFi(view: View) {

        dataScope.launch {
            val x = bleDataWorker.getData()
            Log.e("ga", "${x.wave.len}")
        }
    }
}
