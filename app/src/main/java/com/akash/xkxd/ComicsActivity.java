package com.akash.xkxd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.akash.xkxd.util.DataBaseHelper;
import com.akash.xkxd.util.TouchImageView;
import com.akash.xkxd.util.Util;
import com.akash.xkxd.util.XkcdComics;
import com.akash.xkxd.util.XkcdJsonData;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ComicsActivity extends AppCompatActivity implements ImageFragment.OnImgDownloadListener{

    private static final String TAG = "ComicsActivity";
    private static ViewPager viewPager;
    private static ComicsAdapter mAdapter;
    private static ActionBar mActionBar;
    private static DataBaseHelper mDbHelper;
    private static ProgressDialog mProgressDialog;
//    private static ArrayList<XkcdData> mComics;
//    private int maxComicNumber;
//    private android.support.v7.app.ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_pager_layout);
        viewPager = (ViewPager) findViewById(R.id.comics_view_pager);

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

//        mComics = mDbHelper.getAllComics();

        mAdapter = new ComicsAdapter(getSupportFragmentManager(), mDbHelper.getAllComics());
        if (viewPager == null)
            Log.d(TAG, "onCreate: viewpager null");
        viewPager.setAdapter(mAdapter);

        viewPager.setOffscreenPageLimit(2);

        Log.d(TAG, "onCreate: ComicsActivity");
        int num = getIntent().getIntExtra(getPackageName()+".NUMBER",0);

//        maxComicNumber = mDbHelper.getMaxNumber();

        if (num == 0)
            num = mDbHelper.getMaxNumber();

//        mAdapter.setMovieList(mComics);
        viewPager.setCurrentItem(num);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                XkcdData comic = mDbHelper.getComic(position);
                if (comic != null){
                    mActionBar.setTitle(comic.getTitle());
                } else {
                    mActionBar.setTitle("#"+position);
                }
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
//        mDbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onImgDownload(XkcdData comic) {
        int pos = viewPager.getCurrentItem();

        if (mDbHelper.getComic(pos) != null){
            mActionBar.setTitle(mDbHelper.getComic(pos).getTitle());
        }
    }

    Comparator<XkcdData> comparator = new Comparator<XkcdData>() {
        @Override
        public int compare(XkcdData o1, XkcdData o2) {
            if(o1.getNum()==o2.getNum())
                return 0;
            else if(o1.getNum()>o2.getNum())
                return 1;
            else
                return -1;
        }
    };

    public class ComicsAdapter extends FragmentStatePagerAdapter {
        private int maxNumber;
        private List<XkcdData> mComics;
//        private final android.support.v7.app.ActionBar mActionBar;

        public ComicsAdapter(FragmentManager fragmentManager, ArrayList<XkcdData> comics) {
            super(fragmentManager);
            this.mComics = comics;
            maxNumber = Collections.max(mComics, comparator).getNum();
            Log.d(TAG, "ComicsAdapter: "+maxNumber);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.init(mDbHelper, position, mActionBar, ComicsActivity.this);
        }

        @Override
        public int getCount() {
            return  (mComics == null) ? 0 : maxNumber+1;
        }

        public void UpdateComics(ArrayList<XkcdData> comics) {
            mComics.addAll(comics.subList(mComics.size()-1, comics.size()));
            maxNumber = Collections.max(mComics, comparator).getNum();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comics_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try{
                    int num = Integer.parseInt(query);
                    viewPager.setCurrentItem(num);
                    MenuItem searchMenuItem = menu.findItem(R.id.action_search);
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        switch (item.getItemId()) {
            //noinspection SimplifiableIfStatement
            case R.id.action_explain:
                String url = "https://www.explainxkcd.com/wiki/index.php/" + mDbHelper.getComic(viewPager.getCurrentItem()).getNum();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            case R.id.action_share:
                doShare(mDbHelper.getComic(viewPager.getCurrentItem()));
                return true;
            case R.id.action_browse:
                Intent intent = new Intent(this, ListViewActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_get_latest:
                getLatestComic(ComicsActivity.this, "https://xkcd.com/");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static void getLatestComic(final Context context, String url) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("loading");
        mProgressDialog.show();

        Log.d(TAG, "getRetrofitObject: "+url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);

        Call<XkcdJsonData> call = service.getLatestComic();

        call.enqueue(new Callback<XkcdJsonData>() {
            @Override
            public void onResponse(Call<XkcdJsonData> call, Response<XkcdJsonData> response) {
                if (response.body() != null){
                    XkcdJsonData d = response.body();
                    Log.d(TAG, "onResponse: "+d.getNum()+d.getTitle());
                    XkcdData comic = new XkcdData(Integer.parseInt(d.getNum()),
                            d.getDay(), d.getMonth(), d.getYear(), d.getTitle(), d.getAlt(), d.getImg());

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
                        Log.d(TAG, "onResponse: adding");
                        db.insertXkcd(comic);
                        mAdapter.UpdateComics(mDbHelper.getAllComics());
                    }
                    viewPager.setCurrentItem(comic.getNum());
                    db.close();
                } else {
                    Log.d(TAG, "onResponse: Null!");
                }
                mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<XkcdJsonData> call, Throwable t) {
                mProgressDialog.dismiss();
            }
        });
    }


    public void doShare(XkcdData comic) {
        // populate the share intent with data
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File sdCard = Environment.getExternalStorageDirectory();
        shareIntent.putExtra(Intent.EXTRA_TEXT, comic.getAlt());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
                new File(sdCard.getAbsolutePath()+"/XKCD/"+comic.getNum()+".png")));
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

}
