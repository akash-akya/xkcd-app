package com.akash.xkxd;

import android.app.ActionBar;
import android.content.Context;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akash.xkxd.util.DataBaseHelper;
import com.akash.xkxd.util.TouchImageView;
import com.akash.xkxd.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ComicsActivity extends ActionBarActivity {

    private static final String TAG = "ComicsActivity";
    private ViewPager viewPager;
    private ComicsAdapter mAdapter;
    private DataBaseHelper mDbHelper;
    private android.support.v7.app.ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_pager_layout);
        viewPager = (ViewPager) findViewById(R.id.comics_view_pager);
        mAdapter = new ComicsAdapter(this, getSupportActionBar());
        viewPager.setAdapter(mAdapter);

        mActionBar = getSupportActionBar();

        if (mDbHelper != null)
            mDbHelper.close();

        mDbHelper = new DataBaseHelper(this);
        try {
            mDbHelper.createDataBase();

        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            mDbHelper.openDataBase();
        } catch(SQLException sqle){
            throw sqle;
        }


        final ArrayList<XkcdData> mComics = mDbHelper.getAllComics();

        int num = getIntent().getIntExtra(getPackageName()+".NUMBER",0);

        mAdapter.setMovieList(mComics);
        viewPager.setCurrentItem(getIndex(mComics, num));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mActionBar.setTitle(mComics.get(position).getTitle());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int pos) {
                // TODO Auto-generated method stub
            }
        });
    }

    private int getIndex(ArrayList<XkcdData> mComics, int num) {
        for (int i=0; i<mComics.size(); i++){
            if (mComics.get(i).getNum() == num)
                return i;
        }
        return -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    public static class ComicsAdapter extends PagerAdapter {
        private final android.support.v7.app.ActionBar mActionBar;
        private List<XkcdData> mComics;
        private LayoutInflater mInflater;
        private Context mContext;

        public ComicsAdapter(Context context, android.support.v7.app.ActionBar actionBar) {
            this.mContext = context;
            this.mInflater = LayoutInflater.from(context);
            this.mComics = new ArrayList<>();
            this.mActionBar = actionBar;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            final View view = mInflater.inflate(R.layout.comic_image_view, container, false);
            container.addView(view);

            final XkcdData comic = mComics.get(position);

            TouchImageView imgView = (TouchImageView) view.findViewById(R.id.imageView1);

            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext != null) {
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
                    Toast.makeText(mContext, comic.getAlt(),Toast.LENGTH_LONG).show();
                    return false;
                }
            });

            File file = new File(Util.getFilePath(comic.getNum()));
            if(file.exists()){
                imgView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            } else {
                Picasso.with(mContext)
                        .load(comic.getImg())
                        .placeholder(R.color.colorAccent)
                        .into(getTarget(mComics.get(position).getNum(), imgView));
            }

            return view;
        }

        //target to save
        private static Target getTarget(final int num, final TouchImageView imageView){
            Target target = new Target(){

                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "onBitmapLoaded: IMG");
                    imageView.setImageBitmap(bitmap);

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

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            return target;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        public void setMovieList(List<XkcdData> comics) {
            this.mComics.clear();
            this.mComics.addAll(comics);
            // The adapter needs to know that the data has changed. If we don't call this, app will crash.
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mComics == null) ? 0 : mComics.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
