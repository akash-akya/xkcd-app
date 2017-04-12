package com.akash.xkxd;

import android.content.Context;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.akash.xkxd.util.DataBaseHelper;
import com.akash.xkxd.util.TouchImageView;
import com.akash.xkxd.util.Util;
import com.akash.xkxd.util.XkcdData;
import com.akash.xkxd.util.XkcdJsonData;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";
    private static DataBaseHelper sDbHelper;
    private static ActionBar sActionBar;
    private static OnImgDownloadListener sOnImgDownloadListener;
    private int mNum;

    static ImageFragment init(DataBaseHelper dbHelper, int val, ActionBar actionBar,
                              OnImgDownloadListener onImgDownloadListener) {
        ImageFragment frag = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);

        sOnImgDownloadListener = onImgDownloadListener;

        sDbHelper = dbHelper;
        sActionBar = actionBar;

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.comic_image_view, container, false);

        TouchImageView imgView = (TouchImageView) layoutView.findViewById(R.id.imageView1);
        ProgressBar progressBar = (ProgressBar) layoutView.findViewById(R.id.progress_bar);

        imgView.setScaleType(ImageView.ScaleType.CENTER);
        imgView.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);

        final XkcdData comic = sDbHelper.getComic(mNum);

        if (comic == null){
            getRetrofitObject(getContext() ,"https://xkcd.com/", mNum, imgView, progressBar);
        } else{
            setOrLoadComic(getContext(), comic, imgView, progressBar);
        }
        return layoutView;
    }

    static void setOrLoadComic(final Context context, final XkcdData comic, TouchImageView imgView,
                               ProgressBar progressBar) {
        sOnImgDownloadListener.onImgDownload(comic);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null) {
                    if(sActionBar.isShowing()){
                        sActionBar.hide();
                    }else {
                        sActionBar.show();
                    }
                }
            }
        });

        imgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAltDialog(context, comic);
                return false;
            }
        });

        File file = new File(Util.getFilePath(comic.getNum()));
        if(file.exists()){
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imgView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            progressBar.setVisibility(View.GONE);
            imgView.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "setOrLoadComic: Get from web");
            Picasso.with(context)
                    .load(comic.getImg())
                    .placeholder(R.color.accent)
                    .priority(Picasso.Priority.HIGH)
                    .into(getTarget(comic.getNum(), imgView, progressBar));
        }
    }

    static void getRetrofitObject(final Context context, String url, int num, final TouchImageView imgView,
                                  final ProgressBar progressBar) {

        Log.d(TAG, "getRetrofitObject: "+url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);

        Call<XkcdJsonData> call = service.getComic(num);

        call.enqueue(new Callback<XkcdJsonData>() {
            @Override
            public void onResponse(Call<XkcdJsonData> call, Response<XkcdJsonData> response) {
                if (response.body() != null){
                    XkcdJsonData d = response.body();
                    // Log.d(TAG, "onResponse: "+d.getNum()+" - "+d.getTitle());
                    XkcdData comic = new XkcdData(Integer.parseInt(d.getNum()),
                            d.getDay(), d.getMonth(), d.getYear(), d.getTitle(), d.getAlt(),
                            d.getImg(), 0);

                    DataBaseHelper db = new DataBaseHelper(context);
                    try {
                        db.createDataBase();
                    } catch (IOException ioe) {
                        throw new Error("Unable to create database");
                    }

                    try {
                        db.openDataBase();
                    } catch(SQLException sqle){
                        throw sqle;
                    }

                    if (db.getComic(comic.getNum()) == null) {
                        db.insertXkcd(comic);
                    }
                    db.close();
                    setOrLoadComic(context, comic, imgView, progressBar);
                } else {
                    Log.d(TAG, "onResponse: Null!");
                }
            }

            @Override
            public void onFailure(Call<XkcdJsonData> call, Throwable t) {

            }
        });
    }

    //target to save
    private static Target getTarget(final int num, final TouchImageView imageView, final ProgressBar progressBar){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "onBitmapLoaded: IMG");
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageBitmap(bitmap);

                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/XKCD/");
                        if (!folder.exists()) {
                            folder.mkdir();//If there is no folder it will be created.
                        }

                        File file = new File(Util.getFilePath(num));
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed: ");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        return target;
    }

    interface OnImgDownloadListener {
        void onImgDownload(XkcdData comic);
    }

    static void showAltDialog(Context context, XkcdData comic){
        new AlertDialog.Builder(context)
                .setTitle(comic.getTitle()+" ("+comic.getNum()+")")
                .setMessage(comic.getAlt())
                .show();
    }
}
