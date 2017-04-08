package com.akash.xkxd.util;

import android.os.Environment;

import java.io.File;

public abstract class Util {
    public static String getFilePath(int num) {
        String targetFileName = num + ".png";
        File sdCard = Environment.getExternalStorageDirectory();
        return (sdCard.getAbsolutePath()+ "/XKCD/"+targetFileName);
    }
}
