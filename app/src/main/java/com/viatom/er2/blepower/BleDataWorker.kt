package com.viatom.er2.blepower

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.viatom.er2.blething.BleCmd
import com.viatom.er2.blething.BleCmd.getRtData
import com.viatom.er2.blething.BleResponse
import com.viatom.er2.blething.Formatter
import com.viatom.er2.blething.Gua.getPathX
import com.viatom.er2.pkg.StartReadPkg
import com.viatom.er2.utils.CRCUtils
import com.viatom.er2.utils.add
import com.viatom.er2.utils.toUInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.ble.data.Data
import java.io.File
import kotlin.experimental.inv
@ExperimentalUnsignedTypes
class BleDataWorker {
    private var pool: ByteArray? = null
    private val fileChannel = Channel<Int>(Channel.CONFLATED)
    private val RtChannel = Channel<BleResponse.RtData>(Channel.CONFLATED)
    private var fileDataChannel = Channel<ByteArray>(Channel.CONFLATED)
    private val connectChannel = Channel<String>(Channel.CONFLATED)
    private lateinit var myBleDataManager: BleDataManager
    private val dataScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    var pkgTotal = 0;
    var currentPkg = 0;
    var fileData: ByteArray? = null
    var currentFileName = ""
    var result = 1;
    var currentFileSize = 0


    companion object {
        lateinit var gua:Formatter.Er2FileList

      const  val ER2_CMD_GET_INFO = 0xE1
      const  val ER2_CMD_RT_DATA = 0x03
      const  val ER2_CMD_VIBRATE_CONFIG = 0x00
      const  val ER2_CMD_READ_FILE_LIST = 0xF1
      const  val ER2_CMD_READ_FILE_START = 0xF2
      const  val ER2_CMD_READ_FILE_DATA = 0xF3
      const  val ER2_CMD_READ_FILE_END = 0xF4
       val fileProgressChannel = Channel<FileProgress>(Channel.CONFLATED)
    }

    data class FileProgress(
            var name: String = "",
            var progress: Int = 0,
            var success: Boolean = false
    )

    private val comeData = object : BleDataManager.OnNotifyListener {
        override fun onNotify(device: BluetoothDevice?, data: Data?) {
            data?.value?.apply {
                pool = add(pool, this)
            }
            pool?.apply {
                pool = handleDataPool(pool)
            }
        }

    }


    private fun handleDataPool(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }
        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i + 8 + len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == CRCUtils.calCRC8(temp)) {

                val bleResponse = BleResponse.Er2Response(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? =
                    if (i + 8 + len == bytes.size) null else bytes.copyOfRange(
                            i + 8 + len,
                            bytes.size
                    )

                return handleDataPool(tempBytes)
            }
        }

        return bytesLeft
    }





    private fun onResponseReceived(response: BleResponse.Er2Response) {

        when(response.cmd) {

            ER2_CMD_RT_DATA->{
                dataScope.launch {
                    RtChannel.send(BleResponse.RtData(response.content))
                }

            }


            ER2_CMD_READ_FILE_LIST->{
                gua=Formatter.Er2FileList(response.content)
                Log.e("姑姑2",gua.size.toString())

            }

            ER2_CMD_READ_FILE_START->{
                val fileStart = Formatter.Er2FileSize(response.content)
                dataScope.launch {
                    fileChannel.send(fileStart.size)
                }
            }

            ER2_CMD_READ_FILE_END->{
                dataScope.launch {
                    fileChannel.send(0)
                }
            }

            ER2_CMD_READ_FILE_DATA->{
                dataScope.launch {
                    Log.e("sdfklj","${response.content.size}")
                    fileDataChannel.send(response.content)
                }
            }

        }

    }
    
    
    
    
    
    
    private fun sendCmd(bs: ByteArray) {
        myBleDataManager.sendCmd(bs)
    }


    fun initWorker(context: Context, bluetoothDevice: BluetoothDevice?) {
        myBleDataManager = BleDataManager(context)
        myBleDataManager.setNotifyListener(comeData)
        bluetoothDevice?.let {
            myBleDataManager.connect(it)
                .useAutoConnect(true)
                .timeout(10000)
                .retry(15, 100)
                .done {
                    Log.i("BLE", "连接成功了.>>.....>>>>")
                    dataScope.launch {
                        connectChannel.send("yes")
                    }

                }
                .enqueue()
        }
    }

    suspend fun waitConnect() {
        connectChannel.receive()
    }

    suspend fun getFile(name: String): Int {
        mutex.withLock {
            this.currentFileName = name
            val pkg = StartReadPkg(name)
            sendCmd(pkg.buf)
            return fileChannel.receive()
        }
    }

    suspend fun getFileList(){
        mutex.withLock {
            val pkg = BleCmd.getFileList()
            sendCmd(pkg)
        }
    }


    suspend fun getFileStart(b:ByteArray){
        mutex.withLock {
            val pkg = BleCmd.readFileStart(b,0)
            sendCmd(pkg)
        }
    }


    suspend fun getFile(b:ByteArray){
        mutex.withLock {
            val file=Formatter.Er2FileBuf()
            file.fileName=b
            val pkg = BleCmd.readFileStart(b,0)
            sendCmd(pkg)
            file.fileSize=fileChannel.receive()
            Log.e("sdf","sdkjkldsf  ${file.fileSize}")
            file.filePointer=0
            while(file.filePointer<file.fileSize){
                sendCmd(BleCmd.readFileData(file.filePointer))
                val temp=fileDataChannel.receive()
                file.add(temp)
                Log.e("dsfs","${file.filePointer}        ${file.fileSize}")
            }
            sendCmd(BleCmd.readFileEnd())
            fileChannel.receive()
            Log.e("fd","dsf")
            File(getPathX(file.fileNameString)).writeBytes(file.fileData)
        }
    }



    suspend fun getData():BleResponse.RtData{
        mutex.withLock {
            sendCmd(getRtData())
            return RtChannel.receive()
        }
    }

}