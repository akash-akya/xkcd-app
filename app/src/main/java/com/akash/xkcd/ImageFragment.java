package com.akash.xkcd;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.akash.xkcd.database.ComicsDb;
import com.akash.xkcd.database.Xkcd;
import com.akash.xkcd.util.BitmapHelper;
import com.akash.xkcd.util.RequestInterface;
import com.akash.xkcd.util.TouchImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";
    private OnImgDownloadListener mFragmentListener;
    private int mNum;
    private SharedPreferences prefs;
    public static final String appDirectoryName = "XKCD";
    public static final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), appDirectoryName);

    @BindView(R.id.comic_image) TouchImageView comicImage;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    private ComicsDb db;

    static ImageFragment init(int val) {
        ImageFragment frag = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt("val", val);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mFragmentListener = (OnImgDownloadListener) getActivity();

        mNum = (getArguments() != null) ? getArguments().getInt("val") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.comic_image_view, container, false);
        ButterKnife.bind(this, layoutView);

        comicImage.setScaleType(ImageView.ScaleType.CENTER);
        comicImage.setVisibility(View.GONE);
        comicImage.setOnTouchListener(mFragmentListener.getOnImageTouchListener(comicImage));
        progressBar.setVisibility(View.VISIBLE);

        db = ComicsDb.getComicsDb(getActivity());
        final Xkcd comic = db.xkcdDao().getComic(mNum);

        if (comic == null){
            getComic(mNum);
        } else{
            setOrLoadComic(comic);
        }

        if (prefs.getBoolean("night_mode", false)) {
            layoutView.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.background_dark, null));
        }
        return layoutView;
    }

    void setOrLoadComic(final Xkcd comic) {
        mFragmentListener.onImgDownload(comic);
        comicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentListener.onImageTap();
            }
        });

        comicImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAltDialog(getContext(), comic.title+" ("+comic.num+")",comic.alt);
                return false;
            }
        });

        File file = new File(ImageFragment.getFilePath(comic.num));
        if(prefs.getBoolean(MyPreferencesActivity.PREF_OFFLINE_MODE, true) && file.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            setImage(BitmapFactory.decodeFile(file.getAbsolutePath(), options));
        } else {
            Target target = getTarget(comic.num);
            comicImage.setTag(target);
            Picasso.with(getContext())
                    .load(comic.img)
                    .placeholder(R.color.accent)
                    .priority(Picasso.Priority.HIGH)
                    .into(target);
        }
    }

    private void setImage(Bitmap image) {
        comicImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (prefs.getBoolean("night_mode", false)){
            comicImage.setImageBitmap(BitmapHelper.invert(image));
        } else {
            comicImage.setImageBitmap(image);
        }

        progressBar.setVisibility(View.GONE);
        comicImage.setVisibility(View.VISIBLE);
    }

    private void getComic(int num) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://Xkcd.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);

        Call<Xkcd> call = service.getComic(num);
        call.enqueue(onResponse);
    }

    private Callback<Xkcd> onResponse =  new Callback<Xkcd>() {
        @Override
        public void onResponse(Call<Xkcd> call, Response<Xkcd> response) {
            if (response.body() != null) {
                Xkcd comic = response.body();
                if (db.xkcdDao().getComic(comic.num) == null) {
                    db.xkcdDao().insertXkcd(comic);
                }
                setOrLoadComic(comic);
            } else {
                Log.d(TAG, "onResponse: Null!");
            }
        }

        @Override
        public void onFailure(Call<Xkcd> call, Throwable t) {

        }
    };

    void saveImage(final int num, final Bitmap bitmap){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!imageRoot.exists()) {
                    imageRoot.mkdir();//If there is no folder it will be created.
                }

                File file = new File(getFilePath(num));
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

    public static String getFilePath(int num) {
        return (imageRoot.getAbsolutePath()+"/"+num+".png");
    }

    //target to save
    private Target getTarget(final int num){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                setImage(bitmap.copy(Bitmap.Config.ARGB_8888, true));

                Log.d(TAG, "onBitmapLoaded: " + num);

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
        void onImgDownload(Xkcd comic);
        void onImageTap();
        View.OnTouchListener getOnImageTouchListener(TouchImageView comicView);
    }

    static void showAltDialog(Context context, String title, String text){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
}
