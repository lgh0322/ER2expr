package com.viatom.er2.blething

import com.viatom.er2.blething.WaveUtil.byteTomV
import com.viatom.er2.utils.toUInt

@ExperimentalUnsignedTypes
object BleResponse {
    class Er2Response  (var bytes: ByteArray){
        var cmd: Int = (bytes[1].toUInt() and 0xFFu).toInt()
        var pkgType: Byte = bytes[3]
        var pkgNo: Int = (bytes[4].toUInt() and 0xFFu).toInt()
        var len: Int = toUInt(bytes.copyOfRange(5, 7))
        var content: ByteArray = bytes.copyOfRange(7, 7 + len)

    }



    class RtData (var bytes: ByteArray)  {
        var content: ByteArray = bytes
        var param: RtParam = RtParam(bytes.copyOfRange(0, 20))
        var wave: RtWave = RtWave(bytes.copyOfRange(20, bytes.size))

    }


    class RtParam (var bytes: ByteArray)  {
        var hr: Int = toUInt(bytes.copyOfRange(0, 2))
        var sysFlag: Byte = bytes[2]
        var battery: Int = (bytes[3].toUInt() and 0xFFu).toInt()
        var recordTime: Int = 0
        var runStatus: Byte
        var leadOn: Boolean
        // reserve 11

        init {
            if (bytes[8].toUInt() and 0x02u == 0x02u) {
                recordTime = toUInt(bytes.copyOfRange(4, 8))
            }
            runStatus = bytes[8]
            leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
        }
    }


    class RtWave (var bytes: ByteArray){
        var content: ByteArray = bytes
        var len: Int = toUInt(bytes.copyOfRange(0, 2))
        var wave: ByteArray = bytes.copyOfRange(2, bytes.size)
        var wFs : FloatArray? = null

        init {
            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = byteTomV(wave[2 * i], wave[2 * i + 1])
            }
        }
//        private fun byteTomV(a: Byte, b: Byte): Float {
//            if (a == 0xff.toByte() && b == 0x7f.toByte()) return 0f
//            val n = a.toUByte().toInt()+b.toUByte().toInt()*256
//            return (n * (1.0035 * 1800) / (4096 * 178.74)).toFloat()
//        }
    }

}