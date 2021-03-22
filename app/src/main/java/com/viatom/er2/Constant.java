package com.viatom.er2;

import android.content.Context;



import java.io.File;

public class Constant {


    public final static String[] EcgWay = {"Hand-Hand", "Hand-Chest", "1-Lead", "2-Lead"};
    public final static String[] OxyWay = {"Internal", "External", ""};
    public final static String[] TmpWay = {"Body", "Thing", ""};
    public static String filePath;

    public static String getPathX(String s) {
        return filePath + s;
    }

    public static void initVar(Context context) {
        File[] fs = context.getExternalFilesDirs(null);
        if (fs != null && fs.length >= 1) {
            filePath = fs[0].getAbsolutePath() + "/";
        }
    }
}
