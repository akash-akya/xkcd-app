package com.akash.xkcd;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.akash.xkcd.util.DataBaseHelper;
import com.akash.xkcd.util.TouchImageView;
import com.akash.xkcd.util.XkcdData;
import com.akash.xkcd.util.XkcdJsonData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.akash.xkcd.ImageFragment.showAltDialog;


public class ComicsActivity extends AppCompatActivity implements ImageFragment.OnImgDownloadListener{

    private static final String TAG = "ComicsActivity";
    private static final String XKCD_URL = "https://xkcd.com/";
    private static final int GET_COMIC_NUM = 1;
    public static final String ARG_COMIC_NUMBER = "ComicNumber";
    private static ViewPager sViewPager;
    private static ComicsAdapter sAdapter;
    private static ActionBar sActionBar;
    private static DataBaseHelper sDbHelper;
    private static ProgressDialog sProgressDialog;
    private MenuItem mFavoriteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_pager_layout);
        sViewPager = (ViewPager) findViewById(R.id.comics_view_pager);

        sActionBar = getSupportActionBar();

        if (sDbHelper != null)
            sDbHelper.close();
        sDbHelper = new DataBaseHelper(this);
        try {
            sDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        sDbHelper.openDataBase();

        verifyStoragePermissions(this);


        sAdapter = new ComicsAdapter(getSupportFragmentManager(), sDbHelper.getAllComics());

        if (sViewPager == null)
            Log.d(TAG, "onCreate: viewpager null");

        sViewPager.setAdapter(sAdapter);
        sViewPager.setOffscreenPageLimit(2);

        int num = getIntent().getIntExtra(ARG_COMIC_NUMBER, 0);

        if (num == 0)
            num = sDbHelper.getMaxNumber();
        if (num == 0)
            getLatestComic(this);

        sViewPager.setCurrentItem(num);

        sViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                XkcdData comic = sDbHelper.getComic(position);
                if (comic != null){
                    updateActionBarFavorite(mFavoriteMenuItem, comic.getFavorite());
                    sActionBar.setTitle(comic.getTitle());
                } else {
                    updateActionBarFavorite(mFavoriteMenuItem, false);
                    sActionBar.setTitle("#"+position);
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

    @Override
    public void onImgDownload(XkcdData comic) {
        int pos = sViewPager.getCurrentItem();

//        Log.d(TAG, "onImgDownload: Image Downloaded"+comic.getNum());

        if (sDbHelper.getComic(pos) != null){
            sActionBar.setTitle(sDbHelper.getComic(pos).getTitle());
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

        public ComicsAdapter(FragmentManager fragmentManager, ArrayList<XkcdData> comics) {
            super(fragmentManager);
            this.mComics = comics;
            try {
                maxNumber = Collections.max(mComics, comparator).getNum();
            } catch (NoSuchElementException e){
                e.printStackTrace();
                maxNumber = 0;
            }
        }

        @Override
        public Fragment getItem(int position) {
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.comics_view_pager
                    + ":" + position);

            if (page != null) {
                Log.d(TAG, "getItem: page exists :"+position);
                return page;
            }
            return ImageFragment.init(sDbHelper, position, sActionBar, ComicsActivity.this);
        }

        @Override
        public int getItemPosition(Object object) {
            // POSITION_NONE makes it possible to reload the PagerAdapter
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return  (mComics == null) ? 0 : maxNumber+1;
        }

        public void UpdateComics(ArrayList<XkcdData> comics) {
            mComics.addAll(comics.subList(mComics.size(), comics.size()));
            maxNumber = Collections.max(mComics, comparator).getNum();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comics_menu, menu);
        mFavoriteMenuItem = menu.findItem(R.id.action_favorite);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try{
                    int num = Integer.parseInt(query);
                    sViewPager.setCurrentItem(num);
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
        switch (item.getItemId()) {
            case R.id.action_favorite:
                XkcdData comic = sDbHelper.getComic(sViewPager.getCurrentItem());
                if (comic != null) {
                    boolean new_state = !comic.getFavorite();
//                    comic.setFavorite(new_state);
                    long numRows = sDbHelper.setFavorite(comic.getNum(), new_state);
                    Log.d(TAG, "setFavorite: "+numRows);
                    sAdapter.UpdateComics(sDbHelper.getAllComics());
                    updateActionBarFavorite(mFavoriteMenuItem, new_state);
                }
                return true;

            case R.id.action_explain:
                String url = "https://www.explainxkcd.com/wiki/index.php/" + sDbHelper.getComic(sViewPager.getCurrentItem()).getNum();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;

            case R.id.action_share:
                doShare(sDbHelper.getComic(sViewPager.getCurrentItem()));
                return true;

            case R.id.action_browse:
                Intent intent = new Intent(this, ListViewActivity.class);
                intent.putExtra(ListViewActivity.ARG_FAVORITE, false);
                startActivityForResult(intent, GET_COMIC_NUM);
                return true;

            case R.id.action_browse_favorite:
                Intent favIntent = new Intent(this, ListViewActivity.class);
                favIntent.putExtra(ListViewActivity.ARG_FAVORITE, true);
                startActivityForResult(favIntent, GET_COMIC_NUM);
                return true;

            case R.id.action_get_latest:
                getLatestComic(ComicsActivity.this);
                return true;

            case R.id.action_transcript:
                if (sDbHelper.getComic(sViewPager.getCurrentItem()) != null)
                    showAltDialog(this, sDbHelper.getComic(sViewPager.getCurrentItem()));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GET_COMIC_NUM){
            if(resultCode == RESULT_OK){
                int num = data.getIntExtra(ARG_COMIC_NUMBER, -1);
                if(num!= -1){
                    sViewPager.setCurrentItem(num);
                }
            }
        }
    }

    private void updateActionBarFavorite(MenuItem starMenuItem, boolean favorite) {
        if (starMenuItem != null){
            starMenuItem.setIcon(favorite? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    static void getLatestComic(final Context context) {
        sProgressDialog = new ProgressDialog(context);
        sProgressDialog.setMessage("loading");
        sProgressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(XKCD_URL)
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
                            d.getDay(), d.getMonth(), d.getYear(), d.getTitle(),
                            d.getAlt(), d.getImg(), d.getTranscript(),0);

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
                        sAdapter.UpdateComics(sDbHelper.getAllComics());
                    }
                    sAdapter.notifyDataSetChanged();
                    sViewPager.setCurrentItem(comic.getNum());
                    db.close();
                } else {
                    Log.d(TAG, "onResponse: Null!");
                }
                sProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<XkcdJsonData> call, Throwable t) {
                sProgressDialog.dismiss();
            }
        });
    }


    public void doShare(XkcdData comic) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File sdCard = Environment.getExternalStorageDirectory();
        shareIntent.putExtra(Intent.EXTRA_TEXT, comic.getAlt());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
                new File(sdCard.getAbsolutePath()+"/XKCD/"+comic.getNum()+".png")));
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

}