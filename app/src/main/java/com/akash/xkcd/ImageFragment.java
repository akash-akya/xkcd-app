package com.akash.xkcd;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.akash.xkcd.util.BitmapHelper;
import com.akash.xkcd.util.DataBaseHelper;
import com.akash.xkcd.util.TouchImageView;
import com.akash.xkcd.util.Util;
import com.akash.xkcd.util.XkcdData;
import com.akash.xkcd.util.XkcdJsonData;
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
    private ActionBar mActionBar;
    private OnImgDownloadListener sOnImgDownloadListener;
    private int mNum;
    private TouchImageView imgView;
    private ProgressBar progressBar;
    private SharedPreferences prefs;

    static ImageFragment init(DataBaseHelper dbHelper, int val, ActionBar actionBar,
                              OnImgDownloadListener onImgDownloadListener) {
        ImageFragment frag = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        sDbHelper = dbHelper;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ComicsActivity activity = (ComicsActivity) getActivity();
        sOnImgDownloadListener = activity;
        mActionBar = activity.getSupportActionBar();

        mNum = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.comic_image_view, container, false);

        imgView = (TouchImageView) layoutView.findViewById(R.id.imageView1);
        progressBar = (ProgressBar) layoutView.findViewById(R.id.progress_bar);

        imgView.setScaleType(ImageView.ScaleType.CENTER);
        imgView.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);

        final XkcdData comic = sDbHelper.getComic(mNum);
//        Log.d(TAG, "onCreateView: "+mNum + " id: "+imgView.getId());

        if (comic == null){
            getRetrofitObject(getContext() ,"https://xkcd.com/", mNum);
        } else{
            setOrLoadComic(getContext(), comic);
        }

        if (prefs.getBoolean("night_mode", false)) {
            layoutView.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.background_dark, null));
        }
        return layoutView;
    }

    void setOrLoadComic(final Context context, final XkcdData comic) {
        sOnImgDownloadListener.onImgDownload(comic);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null) {
                    if(mActionBar.isShowing()){
                        mActionBar.hide();
                    }else {
                        mActionBar.show();
                    }
                }
            }
        });

        imgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAltDialog(context, comic.getTitle()+" ("+comic.getNum()+")",comic.getAlt());
                return false;
            }
        });

        File file = new File(Util.getFilePath(comic.getNum()));
        if(prefs.getBoolean(MyPreferencesActivity.PREF_OFFLINE_MODE, true) && file.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            setImage(BitmapFactory.decodeFile(file.getAbsolutePath(), options));
        } else {
//            Log.d(TAG, "setOrLoadComic: Get from web");
            Target target = getTarget(comic.getNum());
            imgView.setTag(target);
            Picasso.with(context)
                    .load(comic.getImg())
                    .placeholder(R.color.accent)
                    .priority(Picasso.Priority.HIGH)
                    .into(target);
        }
    }

    private void setImage(Bitmap image) {
//        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);

        imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (prefs.getBoolean("night_mode", false)){
            imgView.setImageBitmap(BitmapHelper.invert(image));
        } else {
            imgView.setImageBitmap(image);
        }

        progressBar.setVisibility(View.GONE);
        imgView.setVisibility(View.VISIBLE);
    }

    void getRetrofitObject(final Context context, String url, int num) {

//        Log.d(TAG, "getRetrofitObject: "+url);
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
//                     Log.d(TAG, "onResponse: "+d.getNum()+" - "+d.getTitle());
                    XkcdData comic = new XkcdData(Integer.parseInt(d.getNum()),
                            d.getDay(), d.getMonth(), d.getYear(), d.getTitle(), d.getAlt(),
                            d.getImg(), d.getTranscript(),0);

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
                    setOrLoadComic(context, comic);
                } else {
                    Log.d(TAG, "onResponse: Null!");
                }
            }

            @Override
            public void onFailure(Call<XkcdJsonData> call, Throwable t) {

            }
        });
    }

    void saveImage(final int num, final Bitmap bitmap){
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

    //target to save
    private Target getTarget(final int num){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//                Log.d(TAG, "onBitmapLoaded: IMG");
                setImage(bitmap.copy(Bitmap.Config.ARGB_8888, true));

                Log.d(TAG, "onBitmapLoaded: " + num + " id: "+imgView.getId());

                if (prefs.getBoolean(MyPreferencesActivity.PREF_OFFLINE_MODE, true)){
                    saveImage(num, bitmap);
                }
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

    static void showAltDialog(Context context, String title, String text){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
}
