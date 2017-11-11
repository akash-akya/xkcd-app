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

import com.akash.xkcd.util.BitmapHelper;
import com.akash.xkcd.util.DataBaseHelper;
import com.akash.xkcd.util.RequestInterface;
import com.akash.xkcd.util.TouchImageView;
import com.akash.xkcd.util.XkcdData;
import com.akash.xkcd.util.XkcdJsonData;
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

        final XkcdData comic = ComicsActivity.sDbHelper.getComic(mNum);

        if (comic == null){
            getRetrofitObject(mNum);
        } else{
            setOrLoadComic(comic);
        }

        if (prefs.getBoolean("night_mode", false)) {
            layoutView.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.background_dark, null));
        }
        return layoutView;
    }

    void setOrLoadComic(final XkcdData comic) {
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
                showAltDialog(getContext(), comic.getTitle()+" ("+comic.getNum()+")",comic.getAlt());
                return false;
            }
        });

        File file = new File(ImageFragment.getFilePath(comic.getNum()));
        if(prefs.getBoolean(MyPreferencesActivity.PREF_OFFLINE_MODE, true) && file.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            setImage(BitmapFactory.decodeFile(file.getAbsolutePath(), options));
        } else {
            Target target = getTarget(comic.getNum());
            comicImage.setTag(target);
            Picasso.with(getContext())
                    .load(comic.getImg())
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

    private void getRetrofitObject(int num) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://xkcd.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);

        Call<XkcdJsonData> call = service.getComic(num);
        call.enqueue(onResponse);
    }

    private Callback<XkcdJsonData> onResponse =  new Callback<XkcdJsonData>() {
        @Override
        public void onResponse(Call<XkcdJsonData> call, Response<XkcdJsonData> response) {
            if (response.body() != null) {
                XkcdJsonData d = response.body();
                XkcdData comic = new XkcdData(Integer.parseInt(d.getNum()),
                        d.getDay(), d.getMonth(), d.getYear(), d.getTitle(), d.getAlt(),
                        d.getImg(), d.getTranscript(),0);

                DataBaseHelper db = ComicsActivity.sDbHelper;

                if (db.getComic(comic.getNum()) == null) {
                    db.insertXkcd(comic);
                }
                setOrLoadComic(comic);
            } else {
                Log.d(TAG, "onResponse: Null!");
            }
        }

        @Override
        public void onFailure(Call<XkcdJsonData> call, Throwable t) {

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
        void onImgDownload(XkcdData comic);
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
