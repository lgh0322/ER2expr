package com.viatom.er2.pkg


import com.viatom.er2.blething.BTConstant
import com.viatom.er2.utils.CRCUtils
import kotlin.experimental.inv

class ReadContentPkg(pkgNum: Int) {
    val buf: ByteArray = ByteArray(BTConstant.COMMON_PKG_LENGTH)

    init {
        buf[0] = 0xAA.toByte()
        buf[1] = BTConstant.CMD_WORD_READ_CONTENT
        buf[2] = BTConstant.CMD_WORD_READ_CONTENT.inv()
        buf[3] = pkgNum.toByte() //Package number
        buf[4] = (pkgNum shr 8).toByte()
        buf[5] = 0 //data chunk size, the default is 0
        buf[6] = 0
        buf[buf.size - 1] = CRCUtils.calCRC8(buf)
    }
}