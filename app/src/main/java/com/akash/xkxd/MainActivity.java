package com.akash.xkxd;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class MainActivity extends ActionBarActivity {
    int num=0;
    SearchView mSearchView;
    MenuItem mSearchMenuItem;
    TextView description;
    SharedPreferences  mPreference;
    private PullToRefreshLayout mPullToRefreshLayout;
    SharedPreferences.Editor ed;
    String PATH;
    int number;
    ImageView logoimg;
    String targetFileName;
    String mPrev,mNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        description= (TextView) findViewById(R.id.textView);
        logoimg = (ImageView) findViewById(R.id.imageView);
        PATH = Environment.getExternalStorageDirectory()+ "/"+"XKCD/";

        mPreference = getSharedPreferences("XKCD_PREF", Context.MODE_PRIVATE);



        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout1);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener( new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        num=0;
                        new Description().execute();
                    }


                })
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

        Button mP = (Button) findViewById(R.id.mPrevious);
        Button mN = (Button) findViewById(R.id.mNextButton);


        mNext = "";
        mPrev = "";

        mP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPrev.length() != 0){
                    num = Integer.parseInt(mPrev);
                    new Description().execute();
                }
            }
        });

        mN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNext.length() != 0){
                    num = Integer.parseInt(mNext);
                    new Description().execute();
                }
            }
        });


        mP.setMovementMethod(LinkMovementMethod.getInstance());
        mN.setMovementMethod(LinkMovementMethod.getInstance());

        mP.setVisibility(View.GONE);
        mN.setVisibility(View.GONE);


        if(!mPreference.contains("latest")){
            ed = mPreference.edit();
            ed.putInt("latest",0);
            ed.commit();
            description.setText("Swipe down to refresh.");
        }else {
            num = mPreference.getInt("latest",0);
            new Description().execute();
//            targetFileName = Integer.toString(number)+".png";
//            Bitmap bMap = BitmapFactory.decodeFile(PATH+targetFileName);
//            Log.w("XKCD","File : "+PATH+targetFileName);
//            description.setText(mPreference.getString("description_"+Integer.toString(number),""));
//            setTitle(mPreference.getString("title_"+Integer.toString(number),"")+" (" + number + ")");
//
//            logoimg.setImageBitmap(bMap);
        }


    }



    private class Description extends AsyncTask<Void, Void, Void> {

        Bitmap myBitmap;
        String mDescription,mTitle;
        boolean mDuplicate;
        String message;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mPullToRefreshLayout.setRefreshing(true);
            message="";
            mDuplicate = false;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Document document;
            String iUrl = "http://xkcd.com/";
            mNext = "";
            mPrev = "";

            targetFileName = num + ".png";

            if(num != 0)
                iUrl += num + "/";
            Log.w("XKCD",iUrl);

            File file = new File(PATH+targetFileName);
            if(file.exists() && num!=0){
                mDuplicate = true;
                number = num;
                return null;
            }


            try {

                // Connect to the web site
                try {
                    document = Jsoup.connect(iUrl).get();
                }catch (UnknownHostException e){
                    e.printStackTrace();
                    message = "please check your internet connection";
                    return null;
                }catch (HttpStatusException e){
                    e.printStackTrace();
                    message = "Invalid input !";
                    return null;
                }

                // Using Elements to get the class data
                Elements img = document.select("div[id=comic] img[src]");
                // Locate the src attribute
                String imgSrc = img.attr("src");
                mDescription = img.attr("title");

                mTitle = document.select("div[id=ctitle]").text();

                Elements id = document.select("a[rel=prev]");
                String idnum = id.attr("href");

                number = Integer.parseInt(idnum.replaceAll("[^0-9]","")) + 1 ;
                idnum = Integer.toString(number);
                targetFileName = idnum + ".png";

                file = new File(PATH+targetFileName);
                if( num == 0 && file.exists()){
                    mDuplicate = true;
                    return null;
                }

                ed = mPreference.edit();

                if(number > mPreference.getInt("latest",0)){
                    ed.putInt("latest",number);
                }

                ed.putString("title_"+idnum,mTitle);
                ed.putString("description_"+idnum,mDescription);
                ed.commit();

                mPrev = "";
                mNext = "";

                Elements el = document
                        .select("ul[class=comicNav] a[rel=prev]");

                for( Element urlElement : el ) {
                    urlElement.attr("href", "com.akash.xkxd://" + urlElement.attr("href").replaceAll("[^0-9]",""));
                    mPrev = urlElement.toString().replaceAll("[^0-9]","");
                    Log.w("mPrev ",mPrev);
                    break;
                }

                el = document
                        .select("ul[class=comicNav] a[rel=next]");

                for( Element urlElement : el ) {
                    urlElement.attr("href", "com.akash.xkxd://" + urlElement.attr("href").replaceAll("[^0-9]",""));
                    mNext = urlElement.toString().replaceAll("[^0-9]","");
                    Log.w("mNext ",mNext);
                    break;
                }


                // Download image from URL
                InputStream input = new java.net.URL(imgSrc).openStream();
                // Decode Bitmap
                myBitmap = BitmapFactory.decodeStream(input);


                File folder = new File(PATH);
                if(!folder.exists()){
                    folder.mkdir();//If there is no folder it will be created.
                }

                input = new BufferedInputStream(new URL(imgSrc).openStream());
                OutputStream output = new FileOutputStream(PATH+targetFileName);
                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

//                Log.w("akashXkcd",imgSrc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            // Set description into TextView


            Button mP = (Button) findViewById(R.id.mPrevious);
            Button mN = (Button) findViewById(R.id.mNextButton);

            if(message.length()!=0) {

                AlertDialog.Builder mMessage = new AlertDialog.Builder(MainActivity.this);
                mMessage.setMessage(message);
                mMessage.setTitle("Error");
                mMessage.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mMessage.show();


            }

            else if(mDuplicate) {

                if(num == 0){
                    Toast.makeText(getApplicationContext(), "You already have latest content!", Toast.LENGTH_SHORT).show();
                    mN.setVisibility(View.INVISIBLE);
                }else if(num >= mPreference.getInt("latest",0)){
                    mN.setVisibility(View.INVISIBLE);
                } else {
//                    mN.setText(Html.fromHtml("<a href='http://xkcd.com/" +(number+1)+">Next</a>"));
                    mNext = Integer.toString(number+1);
                    mN.setVisibility(View.VISIBLE);
                }

                if(number > 1) {
//                    mP.setText(Html.fromHtml("<a href='http://xkcd.com/" +(number-1)+">Prev</a>"));
                    mPrev = Integer.toString(number-1);
                    mP.setVisibility(View.VISIBLE);
                }else {
                    mP.setVisibility(View.INVISIBLE);
                }

                targetFileName = Integer.toString(number) + ".png";
                Bitmap bMap = BitmapFactory.decodeFile(PATH+targetFileName);
                Log.w("XKCD","File : "+PATH+targetFileName);
                description.setText(mPreference.getString("description_"+Integer.toString(number),""));
                setTitle(mPreference.getString("title_"+Integer.toString(number),"")+" (" + number + ")");
                logoimg.setImageBitmap(bMap);
            } else {

                if(mNext.length()!=0) {
                    mN.setVisibility(View.VISIBLE);
                } else {
                    mN.setVisibility(View.INVISIBLE);
                }
                if(mPrev.length()!=0) {
                    mP.setVisibility(View.VISIBLE);
                }else {
                    mP.setVisibility(View.INVISIBLE);
                }

                description.setText(mDescription);
                setTitle(mTitle+" (" + number + ")");
                logoimg.setImageBitmap(myBitmap);
            }

            mPullToRefreshLayout.setRefreshing(false);
            mPullToRefreshLayout.setRefreshComplete();
        }


    }



    //////////////////// menu /////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        final Menu mMenu=menu;

        mSearchMenuItem = menu.findItem(R.id.search3);
        mSearchView = (SearchView) mSearchMenuItem.getActionView(); //(SearchView) MenuItemCompat.getActionView(mSearchMenuItem);


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                /**
                 * hides and then unhides search tab to make sure keyboard disappears when query is submitted
                 */
//                String query="";
                mMenu.findItem(R.id.search3).collapseActionView();
                try {
                    num = Integer.parseInt(query);
                    new Description().execute();

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.w("akashXkcd",query);
                }


                return true;
            }

        });


//        MenuItem share_item =  menu.findItem(R.id.menu_item_share);
//        share = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
//        share.setShareIntent(doShare());
//        share = (ShareActionProvider) share_item.getActionProvider().;
        return true;

    }

    public void doShare() {
        // populate the share intent with data
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String mAbsPath =  new File(PATH+targetFileName).getAbsolutePath();

        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPreference.getString("description_"+Integer.toString(number),""));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(PATH+targetFileName)));
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
//        return shareIntent;
//        startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), Navigator.REQUEST_SHARE_ACTION);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                AboutApp.Show(MainActivity.this);
                return true;

            case R.id.menu_item_share:
                doShare();
                return true;

            case R.id.action_whatif:

                Intent  intent = new Intent(this,WhatIf.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
