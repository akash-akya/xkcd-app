package com.akash.xkxd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

class GetXkcd extends AsyncTask<Void, Void, Void> {
    public static final int INVALID_NUM = 1;
    public static final int DUPLICATE = 2;
    private static final String TAG = "GetXkcd";

    private int mNum;
    private XkcdData mXkcdData;
    private Context mContext;
    private OnCompletionListener mListener;
    private int mStatus;
    private DataBaseHelper mDbHelper;

    public GetXkcd(int num, Context context, OnCompletionListener onCompletionListener) {
        Log.d(TAG, "GetXkcd: "+num);
        mNum = num;
        mContext = context;
        mListener = onCompletionListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mDbHelper = new DataBaseHelper(mContext);
        try {
            mDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        mDbHelper.openDataBase();
        mListener.onPreStart();
//            mPullToRefreshLayout.setRefreshing(true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        File file = new File(Util.getFilePath(mNum));

        if(file.exists()){
            mStatus = DUPLICATE;
            mXkcdData = mDbHelper.getComic(mNum);
            if (mXkcdData == null) {
                Log.e(TAG, "doInBackground: oooops!");
                return null;
            }
            Log.d(TAG, "doInBackground: "+file.getAbsolutePath());
            BitmapFactory.decodeFile(file.getAbsolutePath());
        }else {
            String url = String.format("http://xkcd.com/%sinfo.0.json", (mNum!=0)? mNum + "/" : "");
            Log.w("XKCD", url);

            String in = getJson(url);
//            Log.d(GetXkcd.class.getSimpleName(), "doInBackground: "+in);
            try {
                JSONObject json = new JSONObject(in);
                mXkcdData = new XkcdData(Integer.parseInt(json.getString("num")),
                        json.getString("day"),
                        json.getString("month"),
                        json.getString("year"),
                        json.getString("title"),
                        json.getString("alt"),
                        json.getString("img"));

                Log.w(MainActivity.class.getName(), mXkcdData.getNum() +
                        mXkcdData.getTitle());

            } catch (JSONException e) {
                e.printStackTrace();
//                    message = "Invalid input !";
                return null;
            }
            mNum = mXkcdData.getNum();

            if(file.exists()){
                mStatus = DUPLICATE;
                mXkcdData = mDbHelper.getComic(mNum);
                if (mXkcdData == null) {
                    Log.e(TAG, "doInBackground: oooops!");
                    return null;
                }
                BitmapFactory.decodeFile(Util.getFilePath(mXkcdData.getNum()));
            } else {
                if (mDbHelper.getComic(mXkcdData.getNum()) == null)
                    mDbHelper.insertXkcd(mXkcdData);

                Log.d(GetXkcd.class.getSimpleName(), "doInBackground: "+mXkcdData.getImg());
                try {
                    downloadFile(mXkcdData.getImg());
                } catch (IOException e){
                    Log.d(TAG, "doInBackground: cant download img");
                }
            }
        }
        return null;
    }

    private void downloadFile(String imgSrc) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/XKCD/");
        if (!folder.exists()) {
            folder.mkdir();//If there is no folder it will be created.
        }

        BufferedInputStream input = new BufferedInputStream(new URL(imgSrc).openStream());

        OutputStream output = new FileOutputStream(Util.getFilePath(mNum));
        byte data[] = new byte[1024];
        long total = 0;
        int count;
        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
        }
        output.flush();
        output.close();

        // Decode Bitmap
//        Bitmap comicBitmap = BitmapFactory.decodeStream(input);

        input.close();
    }

    public String getJson(String iUrl) {
        DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httppost = new HttpPost(iUrl);
        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            // Oops
        }
        finally {
            try{
                if(inputStream != null)
                    inputStream.close();
            } catch(Exception squish) {
                squish.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mDbHelper.close();
        mListener.onCompletion(mXkcdData);
    }

    interface OnCompletionListener {
//        void onCompletion(XkcdData xkcdData);

        void onCompletion(XkcdData xkcdData);
        void onPreStart();
        void OnFail(int status);
    }
}

