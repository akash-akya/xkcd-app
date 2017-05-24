package com.akash.xkcd.util;

import android.graphics.Bitmap;

public class BitmapHelper {
    public static Bitmap invert(Bitmap bitmap) {
        int length = bitmap.getWidth()*bitmap.getHeight();
        int[] array = new int[length];
        bitmap.getPixels(array,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        for (int i=0;i<length;i++){
            // If the bitmap is in ARGB_8888 format
            array[i] = 0xFF000000 | ~array[i];
        }
        bitmap.setPixels(array,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        return bitmap;
    }

}
