package com.akash.xkcd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.akash.xkcd.database.Xkcd;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by akash on 12/11/17.
 */

class ComicImageProvider {
    private static final String appDirectoryName = "XKCD";
    private static final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), appDirectoryName);
    private static final String TAG = ComicImageProvider.class.getSimpleName();

    private Target target;
    private final OnImageGetListener onImageGetListener;
    private final Context context;
    private final Xkcd comic;
    private final boolean saveImage;

    ComicImageProvider(Xkcd comic, Context context, boolean saveImage, OnImageGetListener onImageGetListener){
        this.onImageGetListener = onImageGetListener;
        this.context = context.getApplicationContext();
        this.comic = comic;
        this.saveImage = saveImage;
    }

    void getImage(){
        File file = new File(getFilePath(comic.num));
        if(saveImage && file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            onImageGetListener.onImageGet(BitmapFactory.decodeFile(file.getAbsolutePath(), options), comic);
            Log.d(TAG, "loaded form cache: "+file.getAbsolutePath());
        } else {
            Log.d(TAG, "Start loading web: "+comic.num);
            this.target = getTarget(comic);
            Picasso.with(context)
                    .load(comic.img)
                    .placeholder(R.color.accent)
                    .into(target);
        }
    }

    //target to save
    private Target getTarget(final Xkcd comic){
        return new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                onImageGetListener.onImageGet(bitmap.copy(Bitmap.Config.ARGB_8888, true), comic);
                Log.d(TAG, "onBitmapLoaded: " + comic.num);

                if (saveImage) {
                    saveImage(comic.num, bitmap);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed: "+comic.num);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }


    private static String getFilePath(int num) {
        return String.format(Locale.US,"%s/%d.jpg", imageRoot.getAbsolutePath(),num);
    }

    private void saveImage(final int number, final Bitmap bitmap){
        new Thread(new Runnable() {
            @Override
            public void run() {
            if (!imageRoot.exists()) {
                imageRoot.mkdir();//If there is no folder it will be created.
            }

            File file = new File(getFilePath(number));
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                ostream.flush();
                ostream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            }
        }).start();
    }

    static Uri getImagePath(int number) {
        return Uri.fromFile(new File(getFilePath(number)));
    }

    interface OnImageGetListener {
        void onImageGet(Bitmap image, Xkcd comic);
    }
}
