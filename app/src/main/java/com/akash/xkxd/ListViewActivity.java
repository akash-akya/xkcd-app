package com.akash.xkxd;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.akash.xkxd.util.DataBaseHelper;
import com.akash.xkxd.util.XkcdJsonData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ListViewActivity extends ActionBarActivity implements ComicsListRecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = "ListViewActivity";

//    private List<String> mFilesList;
//    private ListView comicsListView;
//    private SwipeRefreshLayout swipeLayout;

    private static DataBaseHelper mDbHelper;
    private ComicsListRecyclerViewAdapter myAdapter;
    private DateFormat mDateFormat;
    private RecyclerView mRecyclerView;
    private ArrayList<XkcdData> mComics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_list);

        setTitle("XKCD Comics");

        verifyStoragePermissions(this);

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


        mComics = mDbHelper.getAllComics();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_comics);
        final String format = Settings.System.getString(getContentResolver(), Settings.System.DATE_FORMAT);
        if (TextUtils.isEmpty(format)) {
            mDateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
        } else {
            mDateFormat = new SimpleDateFormat(format);
        }
        Log.d(TAG, "onCreate: "+ mComics.size());
        myAdapter = new ComicsListRecyclerViewAdapter(mComics, mDateFormat, this);
        mRecyclerView.setAdapter(myAdapter);

/*        for (int i=0; i<10; i++){
            getRetrofitObject("https://xkcd.com/", i);
        }*/
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    protected void onResume() {
        ArrayList<XkcdData> t = mDbHelper.getAllComics();
        if (t.size() > mComics.size()) {
            Log.d(TAG, "onResume: "+mComics.size()+" - "+t.size());
            mComics.addAll(t.subList(mComics.size()-1,t.size()));
            myAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    public static ArrayList<String> getComicNumbers(ArrayList<XkcdData> comics) {
        ArrayList<String> nums = new ArrayList<>();
        for(XkcdData c : comics) {
            nums.add(String.valueOf(c.getNum()));
//            Log.d(TAG, "getComicNumbers: "+c.getNum());
        }
        return nums;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            //noinspection SimplifiableIfStatement
            case R.id.action_settings:
                AboutApp.Show(ListViewActivity.this);
                return true;
            case R.id.action_download_all:
                updateDatabase();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i=1; i<100; i++) {
                try {
                    RequestInterface requestInterface = new Retrofit.Builder()
                            .baseUrl("https://xkcd.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(RequestInterface.class);

                    Call<XkcdJsonData> call = requestInterface.getComic(i);
                    XkcdJsonData d = call.execute().body();
                    Log.d(TAG, "updateDatabase: "+i);

                    XkcdData comic = new XkcdData(Integer.parseInt(d.getNum()),
                            d.getDay(), d.getMonth(), d.getYear(), d.getTitle(), d.getAlt(), d.getImg());

                    DataBaseHelper db = new DataBaseHelper(getBaseContext());
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private void updateDatabase() {
        new DownloadTask().execute();
    }

    @Override
    public void onItemClick(XkcdData comic) {
        Intent intent = new Intent(ListViewActivity.this, ComicsActivity.class);
        intent.putExtra(getPackageName() + ".NUMBER", comic.getNum());
        startActivity(intent);
    }

}
