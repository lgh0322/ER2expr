package com.viatom.er2.blething


object Formatter {
    class Er2FileSize(var bytes: ByteArray) {
        var size: Int =
            (bytes[0].toInt() and 0xff) + (bytes[1].toInt() and 0xff) * 256 + (bytes[2].toInt() and 0xff) * 65536 + (bytes[3].toInt() and 0xff) * 16777216
    }

    class Er2FileList(var bytes: ByteArray) {
        var size: Int = bytes[0].toInt() and 0xff
        var fileList: Array<ByteArray> = Array(size) {
            ByteArray(16)
        }
        var fileListString: Array<String> = Array(size) {
            ""
        }

        init {
            for (k in 0 until size) {
                for ((index, j) in (k * 16 until k * 16 + 16).withIndex()) {
                    fileList[k][index] = bytes[j + 1]
                }
                fileListString[k] = String(fileList[k].copyOfRange(0, 15))
            }
        }
    }


    class Er2FileBuf {
        var fileNameString: String = ""
        var fileName: ByteArray? = null
            set(value) {
                var l: Int = 0;
                for ((index, k) in value!!.withIndex()) {
                    if (k == (0x00.toByte())) {
                        l = index;
                        break;
                    }
                }
                val name = ByteArray(l)
                for (k in 0 until l) {
                    name[k] = value[k]
                }
                fileNameString = String(name)
                field = value
            }
        var fileData: ByteArray = ByteArray(0)
        var filePointer: Int = 0;
        var fileSize: Int = 0;
        fun add(b: ByteArray) {
            fileData = fileData.plus(b)
            filePointer += b.size
        }
    }


}