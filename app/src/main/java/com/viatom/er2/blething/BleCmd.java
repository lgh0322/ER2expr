package com.viatom.er2.blething;


import static com.viatom.er2.utils.CRCUtils.calCRC8;

public class BleCmd {

    public static int ER2_CMD_GET_INFO = 0xE1;
    public static int ER2_CMD_RT_DATA = 0x03;
    public static int ER2_CMD_VIBRATE_CONFIG = 0x00;
    public static int ER2_CMD_READ_FILE_LIST = 0xF1;
    public static int ER2_CMD_READ_FILE_START = 0xF2;
    public static int ER2_CMD_READ_FILE_DATA = 0xF3;
    public static int ER2_CMD_READ_FILE_END = 0xF4;

    public static String ACTION_ER2_INFO = "com.lepu.ble_ER2_info";
    public static String ACTION_ER2_RT_DATA = "com.lepu.ble_ER2_rtData";

    private static int seqNo = 0;

    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }


    public static byte[] getRtData() {
        int len = 1;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x03;
        cmd[2] = (byte) ~0x03;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x7D;  // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        int len = 0;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xE1;
        cmd[2] = (byte) ~0xE1;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0;
        cmd[6] = (byte) 0;
        cmd[7] = calCRC8(cmd);

        addNo();

        return cmd;
    }

    public static byte[] setVibrate(boolean on1, int threshold1, int threshold2) {
        int len = 3;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x04;
        cmd[2] = (byte) ~0x04;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x03;
        cmd[6] = (byte) 0x00;
        if (on1) {
            cmd[7] = (byte) 0x01;
        } else {
            cmd[7] = (byte) 0x00;
        }

        cmd[8] = (byte) threshold1;
        cmd[9] = (byte) threshold2;
        cmd[10] = calCRC8(cmd);
        addNo();
        return cmd;
    }

    public static byte[] getRtWaveData() {
        int len = 1;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x01;
        cmd[2] = (byte) ~0x01;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x7D;  // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = calCRC8(cmd);
        addNo();
        return cmd;
    }


    public static byte[] getVibrateConfig() {
        int len = 0;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) ~0x00;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = calCRC8(cmd);
        addNo();
        return cmd;
    }


    public static byte[] getFileList() {
        int len = 0;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xF1;
        cmd[2] = (byte) ~0xF1;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = calCRC8(cmd);
        addNo();
        return cmd;
    }

    public static byte[] readFileStart(byte[] name, int offset) {
        int len = 20;

        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xF2;
        cmd[2] = (byte) ~0xF2;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x14;
        cmd[6] = (byte) 0x00;
        int k = 0;
        for (k = 0; k < 16; k++) {
            cmd[7 + k] = name[k];
        }
        byte[] temp = intToByteArray(offset);
        for (k = 0; k < 4; k++) {
            cmd[23 + k] = temp[k];
        }
        cmd[27] = calCRC8(cmd);
        addNo();
        return cmd;
    }


    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    public static byte[] readFileData(int addr_offset) {
        int len = 4;
        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xF3;
        cmd[2] = (byte) ~0xF3;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x04;
        cmd[6] = (byte) 0x00;
        int k;
        byte[] temp = intToByteArray(addr_offset);
        for (k = 0; k < 4; k++) {
            cmd[7 + k] = temp[k];
        }

        cmd[11] = calCRC8(cmd);
        addNo();
        return cmd;
    }

    public static byte[] readFileEnd() {
        int len = 0;
        byte[] cmd = new byte[8 + len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xF4;
        cmd[2] = (byte) ~0xF4;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = calCRC8(cmd);
        addNo();
        return cmd;
    }
}
