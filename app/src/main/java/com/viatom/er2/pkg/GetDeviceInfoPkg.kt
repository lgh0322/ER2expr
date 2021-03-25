package com.viatom.er2.pkg


import com.viatom.er2.blething.BTConstant
import com.viatom.er2.utils.CRCUtils
import kotlin.experimental.inv


class GetDeviceInfoPkg() {
    val buf: ByteArray = ByteArray(BTConstant.COMMON_PKG_LENGTH)

    init {
        buf[0] = 0xAA.toByte()
        buf[1] = BTConstant.CMD_WORD_GET_INFO
        buf[2] = BTConstant.CMD_WORD_GET_INFO.inv()
        buf[3] = 0.toByte() //Package number
        buf[4] = 0.toByte()
        buf[5] = 0.toByte()
        buf[6] = 0.toByte()
        buf[buf.size - 1] = CRCUtils.calCRC8(buf)
    }
}