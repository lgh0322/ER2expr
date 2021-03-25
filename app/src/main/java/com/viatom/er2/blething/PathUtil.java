package com.viatom.er2.blething;

import android.content.Context;

import java.io.File;

public class PathUtil {

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
