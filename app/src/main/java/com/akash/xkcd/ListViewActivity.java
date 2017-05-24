package com.akash.xkcd;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.akash.xkcd.util.DataBaseHelper;
import com.akash.xkcd.util.XkcdData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity implements ComicsListRecyclerViewAdapter.OnItemClickListener {
    private static final String TAG = "ListViewActivity";
    public static final String ARG_FAVORITE = "FAVORITE";

    private static DataBaseHelper sDbHelper;
    private ComicsListRecyclerViewAdapter mAdapter;
    private ArrayList<XkcdData> mComics;
    private boolean mIsFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_list);

        setTitle("XKCD Comics");

        if (sDbHelper != null)
            sDbHelper.close();

        sDbHelper = new DataBaseHelper(this);
        try {
            sDbHelper.createDataBase();

        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            sDbHelper.openDataBase();
        } catch(SQLException sqle){
            throw sqle;
        }

        mIsFavorite = getIntent().getBooleanExtra(ARG_FAVORITE, false);
        Log.d(TAG, "onCreate: "+mIsFavorite);
        mComics = getComicsFromDb(mIsFavorite);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_comics);
        final String format = Settings.System.getString(getContentResolver(), Settings.System.DATE_FORMAT);
        DateFormat mDateFormat;
        if (TextUtils.isEmpty(format)) {
            mDateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
        } else {
            mDateFormat = new SimpleDateFormat(format);
        }
        Log.d(TAG, "onCreate: "+ mComics.size());
        mAdapter = new ComicsListRecyclerViewAdapter(mComics, mDateFormat, this);
        mRecyclerView.setAdapter(mAdapter);

/*        for (int i=0; i<10; i++){
            getRetrofitObject("https://xkcd.com/", i);
        }*/
    }

    @Override
    protected void onResume() {
        ArrayList<XkcdData> t = getComicsFromDb(mIsFavorite);
        if (t.size() > mComics.size()) {
            Log.d(TAG, "onResume: "+mComics.size()+" - "+t.size());
            mComics.addAll(t.subList(mComics.size()-1,t.size()));
            mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sDbHelper.close();
    }

    public static ArrayList<String> getComicNumbers(ArrayList<XkcdData> comics) {
        ArrayList<String> nums = new ArrayList<>();
        for(XkcdData c : comics) {
            nums.add(String.valueOf(c.getNum()));
        }
        return nums;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                AboutApp.Show(ListViewActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(XkcdData comic) {
        Intent intent = new Intent(ListViewActivity.this, ComicsActivity.class);
        intent.putExtra(ComicsActivity.ARG_COMIC_NUMBER, comic.getNum());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
        finish();
    }

    public ArrayList<XkcdData> getComicsFromDb(boolean isFavorite) {
        return isFavorite ? sDbHelper.getFavoriteComics(): sDbHelper.getAllComics();
    }
}
