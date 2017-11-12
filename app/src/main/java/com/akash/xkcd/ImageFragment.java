package com.akash.xkcd;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.akash.xkcd.database.Xkcd;
import com.akash.xkcd.util.BitmapHelper;
import com.akash.xkcd.util.TouchImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";
    private OnFragmentListener mFragmentListener;
//    private Xkcd comic;
    private SharedPreferences prefs;

    @BindView(R.id.comic_image) TouchImageView comicImage;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    private ComicsList comics;
    private ComicImageProvider comicImageProvider;

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
        mFragmentListener = (OnFragmentListener) getActivity();
        comics = ComicsList.getInstance(getActivity());
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

        int num = (getArguments() != null) ? getArguments().getInt("val") : 1;

        comics.getComic(num, new ComicsList.OnGetComicListener() {
            @Override
            public void onGetComic(Xkcd comic) {
                setOrLoadComic(comic);
            }
        });

        if (prefs.getBoolean("night_mode", false)) {
            layoutView.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.background_dark, null));
        }
        return layoutView;
    }

    void setOrLoadComic(final Xkcd comic) {
        if (getActivity() != null){
            comicImage.setOnClickListener(v-> mFragmentListener.onImageTap());

            comicImage.setOnLongClickListener(v -> {
                showAltDialog(getContext(), comic.title+" ("+comic.num+")",comic.alt);
                return false;
            });

            boolean offlineMode = prefs.getBoolean(MyPreferencesActivity.PREF_OFFLINE_MODE, true);
            comicImageProvider = new ComicImageProvider(comic, getActivity(), offlineMode, onImageGetListener);
            comicImageProvider.getImage();
        }
    }

    ComicImageProvider.OnImageGetListener onImageGetListener = this::setImage;

    private void setImage(Bitmap image, Xkcd comic) {
        comicImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (prefs.getBoolean("night_mode", false)){
            comicImage.setImageBitmap(BitmapHelper.invert(image));
        } else {
            comicImage.setImageBitmap(image);
        }

        progressBar.setVisibility(View.GONE);
        comicImage.setVisibility(View.VISIBLE);
    }

    interface OnFragmentListener {
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
