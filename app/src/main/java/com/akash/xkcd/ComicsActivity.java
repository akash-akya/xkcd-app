package com.akash.xkcd;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.akash.xkcd.database.Xkcd;
import com.akash.xkcd.util.TouchImageView;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.akash.xkcd.ImageFragment.showAltDialog;


public class ComicsActivity extends AppCompatActivity implements ImageFragment.OnFragmentListener {
    private static final String TAG = "ComicsActivity";
    private static final String XKCD_URL = "https://Xkcd.com/";
    private static final int GET_COMIC_NUM = 1;
    public static final String ARG_COMIC_NUMBER = "ComicNumber";
    private static final int ACTIVITY_PREF = 2;
    private ComicsPageAdapter mAdapter;
    private ActionBar mActionBar;
    private int swipeStartOffset;
    private int swipeEndOffset;
    private MenuItem mFavoriteMenuItem;
    ProgressDialog mProgressDialog;

    @BindView(R.id.comics_view_pager) ViewPager mPager;
    @BindView(R.id.swiperandom) SwipeRefreshLayout mSwipeRandom;
    private ComicsList comicsList;
//    private ComicsDb db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_pager_layout);
        ButterKnife.bind(this);

        mActionBar = getSupportActionBar();

        verifyStoragePermissions(this);

//        db = ComicsDb.getComicsDb(this);
        comicsList = ComicsList.getInstance(getApplicationContext());

        mAdapter = getComicsPagerAdapter();

        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(2);

        int num = getIntent().getIntExtra(ARG_COMIC_NUMBER, 0);
        if (num == 0)
            num = comicsList.getMaxNumber();
        if (num == 0)
            getLatestComic(this);

        swipeStartOffset = mSwipeRandom.getProgressViewStartOffset();
        swipeEndOffset = mSwipeRandom.getProgressViewEndOffset();

        // Initialize the actionbar static height
        mSwipeRandom.setProgressViewOffset(false, getActionBarStaticHeight()+ swipeStartOffset,
                getActionBarStaticHeight()+ swipeEndOffset);

        mSwipeRandom.setNestedScrollingEnabled(true);
        mSwipeRandom.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int max = (mAdapter.getMaxNumber() <1000) ? 1000 : mAdapter.getMaxNumber()-1;
                mPager.setCurrentItem(new Random().nextInt(max)+1, true);
                mSwipeRandom.setRefreshing(false);
            }
        });

        mPager.addOnPageChangeListener(onPageChangeListener);
        mPager.setCurrentItem(num);

        // Manually call the onPageSelected for the first time.
        // See link: https://stackoverflow.com/a/20292064
        mPager.post(new Runnable(){
            @Override
            public void run() {
                onPageChangeListener.onPageSelected(mPager.getCurrentItem());
            }
        });
    }

    private ComicsPageAdapter getComicsPagerAdapter() {
        return new ComicsPageAdapter(getSupportFragmentManager(), comicsList.getMaxNumber()) {
            @Override
            public ImageFragment getItem(int position) {
                return ImageFragment.init(position);
            }
        };
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            updateActionBarFavorite(mFavoriteMenuItem, false);
            mActionBar.setTitle("#"+position);
            comicsList.getComic(position, new ComicsList.OnGetComicListener() {
                @Override
                public void onGetComic(Xkcd comic) {
                    updateActionBarFavorite(mFavoriteMenuItem, comic.isFavorite());
                    mActionBar.setTitle(comic.title);
                }
            });
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int pos) {
            enableDisableSwipeRefresh( pos != ViewPager.SCROLL_STATE_DRAGGING);
        }
    };

    private int getActionBarStaticHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        mSwipeRandom.setEnabled(enable);
    }

    @Override
    public void updateActionBar(Xkcd comic) {
        int pos = mPager.getCurrentItem();

        if (comic.num == pos){
            mActionBar.setTitle(comic.title);
            updateActionBarFavorite(mFavoriteMenuItem, comic.isFavorite());
        }
    }

    @Override
    public void onImageTap() {
        if(mActionBar.isShowing()){
            mActionBar.hide();
        } else {
            mActionBar.show();
        }
        mSwipeRandom.setProgressViewOffset(false, mActionBar.getHideOffset()+ swipeStartOffset,
                mActionBar.getHideOffset()+ swipeEndOffset);
    }

    /**
     * Don't enable swipe if we are in zoomed image view
     * */
    @Override
    public View.OnTouchListener getOnImageTouchListener(final TouchImageView comicView) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        mSwipeRandom.setEnabled(comicView.getZoomedRect().top == 0.0);
                }
                return false;
            }
        };
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
                    mPager.setCurrentItem(num);
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
                comicsList.getComic(mPager.getCurrentItem(), new ComicsList.OnGetComicListener() {
                    @Override
                    public void onGetComic(Xkcd comic) {
                        boolean newState = !(comic.isFavorite());
                        comic.setFavorite(newState);
                        comicsList.setComic(comic);
                        mAdapter.notifyDataSetChanged();
                        updateActionBarFavorite(mFavoriteMenuItem, newState);
                    }
                });
                return true;

            case R.id.action_explain:
                comicsList.getComic(mPager.getCurrentItem(), new ComicsList.OnGetComicListener() {
                    @Override
                    public void onGetComic(Xkcd comic) {
                        String url = "https://www.explainxkcd.com/wiki/index.php/" + comic.num;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                return true;

            case R.id.action_open_browser:
                comicsList.getComic(mPager.getCurrentItem(), new ComicsList.OnGetComicListener() {
                    @Override
                    public void onGetComic(Xkcd comic) {
                        String xkcdUrl = "https://www.Xkcd.com/" + comic.num;
                        Intent xkcdBrowser = new Intent(Intent.ACTION_VIEW);
                        xkcdBrowser.setData(Uri.parse(xkcdUrl));
                        startActivity(xkcdBrowser);
                    }
                });
                return true;

            case R.id.action_share:
                comicsList.getComic(mPager.getCurrentItem(), new ComicsList.OnGetComicListener() {
                    @Override
                    public void onGetComic(Xkcd comic) {
                        doShare(comic);
                    }
                });
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

            case R.id.action_preference:
                Intent preference = new Intent(this, MyPreferencesActivity .class);
                startActivityForResult(preference, ACTIVITY_PREF);
                return true;

            case R.id.action_transcript:
                comicsList.getComic(mPager.getCurrentItem(), new ComicsList.OnGetComicListener() {
                    @Override
                    public void onGetComic(Xkcd comic) {
                        showAltDialog(ComicsActivity.this, comic.title+" ("+comic.num+")", comic.transcript);
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GET_COMIC_NUM && resultCode == RESULT_OK){
            int num = data.getIntExtra(ARG_COMIC_NUMBER, -1);
            if(num!= -1){
                mPager.setCurrentItem(num);
            }
        } else if (requestCode == ACTIVITY_PREF && resultCode == RESULT_OK) {
            if (data.getIntExtra(MyPreferencesActivity.PREF_NIGHT_MODE, 0) == 1){
                int num = mPager.getCurrentItem();
                mAdapter = getComicsPagerAdapter();
                mPager.setAdapter(mAdapter);
                mPager.setCurrentItem(num);
            }
        }
    }

    private void updateActionBarFavorite(MenuItem starMenuItem, boolean isFavorite) {
        if (starMenuItem != null){
            starMenuItem.setIcon(isFavorite? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
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

    public void getLatestComic(final Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("loading");
        mProgressDialog.show();

        comicsList.getLatestComic(new ComicsList.OnGetComicListener() {
            @Override
            public void onGetComic(Xkcd comic) {
                mAdapter.setMaxNumber(comicsList.getMaxNumber());
                mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(comic.num);
                mProgressDialog.dismiss();
            }
        });
    }

    public void doShare(Xkcd comic) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, comic.alt);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
                new File(ImageFragment.imageRoot+"/"+comic.num+".png")));
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

}