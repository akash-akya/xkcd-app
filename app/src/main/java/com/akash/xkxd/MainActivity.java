package com.akash.xkxd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends ActionBarActivity {
    int num=0;
    SearchView mSearchView;
    MenuItem mSearchMenuItem;
//    TextView description;
    SharedPreferences  mPreference;
//    private PullToRefreshLayout mPullToRefreshLayout;
    SharedPreferences.Editor ed;
    String PATH;
    int number;
    ImageView logoimg;
    String targetFileName;
//    String mPrev,mNext;
    SwipeRefreshLayout swipeLayout;
    PhotoViewAttacher mAttacher;
    int old_num=0;
    LinearLayout mDescContainer;
    TextView DescrTextView;
    ActionBar mActionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

//        description= (TextView) findViewById(R.id.textView);
        logoimg = (ImageView) findViewById(R.id.imageView);
        DescrTextView = (TextView) findViewById(R.id.description);
        mDescContainer = (LinearLayout) findViewById(R.id.desc_container);
        PATH = Environment.getExternalStorageDirectory()+ "/"+"XKCD/";

        mActionBar = getSupportActionBar();

        mAttacher = new PhotoViewAttacher(logoimg);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        mPreference = getSharedPreferences("XKCD_PREF", Context.MODE_PRIVATE);

        mDescContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDescContainer.setVisibility(View.GONE);
                return true;
            }
        });

        mDescContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int newVis = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

                if(mActionBar.isShowing()){
                    mActionBar.hide();

                    findViewById(R.id.buttons).setVisibility(View.GONE);
                    newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;

                    v.setSystemUiVisibility(newVis);
                    findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff000000"));

                }else {
                    mActionBar.show();

                    newVis |= View.SYSTEM_UI_FLAG_VISIBLE  ;
                    v.setSystemUiVisibility(newVis);
                    findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                    findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff222222"));
                }
            }
        });

        logoimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getBaseContext());
                dialog.setTitle("fake!");
                dialog.setMessage("fake again!");
                dialog.show();
            }
        });


        Button mP = (Button) findViewById(R.id.mPrevious);
        Button mN = (Button) findViewById(R.id.mNextButton);


        mP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    num = old_num-1;
                    new Description().execute();
            }
        });

        mN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    num = old_num+1; //Integer.parseInt(mNext);
                    new Description().execute();
            }
        });


        mP.setMovementMethod(LinkMovementMethod.getInstance());
        mN.setMovementMethod(LinkMovementMethod.getInstance());

        mP.setVisibility(View.GONE);
        mN.setVisibility(View.GONE);

        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(mDescContainer.getVisibility() == View.VISIBLE){
                    mDescContainer.setVisibility(View.GONE);
                }else {
                    mDescContainer.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });



        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {


                int newVis = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN ;

                if(mActionBar.isShowing()){
                    mActionBar.hide();

                    findViewById(R.id.buttons).setVisibility(View.GONE);
                    newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE  | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    view.setSystemUiVisibility(newVis);
                    findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff000000"));

                }else {

                    mActionBar.show();
                    newVis |= View.SYSTEM_UI_FLAG_VISIBLE ;
                    view.setSystemUiVisibility(newVis);
                    findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                    findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff222222"));
                }
            }
        });


        num = getIntent().getIntExtra(getPackageName()+".NUMBER",0);

        new Description().execute();

    }

    private class Description extends AsyncTask<Void, Void, Void> {

        Bitmap myBitmap;
        String mDescription,mTitle;
        boolean mDuplicate;
        String message;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            mPullToRefreshLayout.setRefreshing(true);

            targetFileName = num + ".png";


            File file = new File(PATH+targetFileName);
            if(file.exists() && num!=0){
                mDuplicate = true;
                number = num;
                old_num = num;

            }else {
                mDuplicate = false;
                findViewById(R.id.content).setVisibility(View.GONE);
                setTitle("XKCD");
            }

            message="";

        }

        @Override
        protected Void doInBackground(Void... params) {

            if(!mDuplicate) {


                String iUrl = "http://xkcd.com/";

                if (num != 0)
                    iUrl += num + "/info.0.json";
                else iUrl += "info.0.json";

                Log.w("XKCD", iUrl);


                try {

                    String in = readBugzilla(iUrl);
                    String idnum = "";
                    String imgSrc = "";
                    try {
                        JSONObject json = new JSONObject(in);
//                        JSONObject json = js.getJSONObject("responseData");
//                        Log.i(MainActivity.class.getName(), json.toString());
                        Log.w(MainActivity.class.getName(), json.getString("safe_title") +
                                json.getString("img"));
                        mTitle  = json.getString("safe_title");
                        idnum = json.getString("num");
                        imgSrc = json.getString("img");
                        mDescription = json.getString("alt");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message = "Invalid input !";
                        return null;
                    }

//                    if (!idnum.equals("")) {
//                        number = Integer.parseInt(idnum) + 1;
//                        idnum = Integer.toString(number);
//                    } else {
//                        number = 1;
//                        idnum = Integer.toString(number);
//                    }
                    number = Integer.parseInt(idnum);
                    old_num = number;

                    targetFileName = idnum + ".png";

                    File file = new File(PATH + targetFileName);
                    if (num == 0 && file.exists()) {
                        mDuplicate = true;
                        return null;
                    }

                    ed = mPreference.edit();

                    if (number > mPreference.getInt("latest", 0)) {
                        ed.putInt("latest", number);
                    }

                    ed.putString("title_" + idnum, mTitle);
                    ed.putString("description_" + idnum, mDescription);
                    ed.commit();


                    // Download image from URL
                    InputStream input = new java.net.URL(imgSrc).openStream();
                    // Decode Bitmap
                    myBitmap = BitmapFactory.decodeStream(input);


                    File folder = new File(PATH);
                    if (!folder.exists()) {
                        folder.mkdir();//If there is no folder it will be created.
                    }

                    input = new BufferedInputStream(new URL(imgSrc).openStream());
                    OutputStream output = new FileOutputStream(PATH + targetFileName);
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
            }
            return null;

        }

        public String readBugzilla(String iUrl) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(iUrl);//"https://bugzilla.mozilla.org/rest/bug?assigned_to=lhenry@mozilla.com");
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e("XKCD ", "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set description into TextView


            Button mP = (Button) findViewById(R.id.mPrevious);
            Button mN = (Button) findViewById(R.id.mNextButton);

            if(message.length()!=0) {

                if(message.equals("Invalid input !") &&
                        mPreference.getInt("latest", 0)+1 == num){
                    Toast.makeText(getApplicationContext(), "You already have latest content!", Toast.LENGTH_SHORT).show();
                }else {

                    AlertDialog.Builder mMessage = new AlertDialog.Builder(MainActivity.this);
                    mMessage.setMessage(message);
                    mMessage.setTitle("Error");
                    mMessage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    mMessage.show();
                }


            } else if(mDuplicate) {

                if(num == 0){
                    Toast.makeText(getApplicationContext(), "You already have latest content!", Toast.LENGTH_SHORT).show();
//                    mN.setVisibility(View.INVISIBLE);
                }
                /*else if(num >= mPreference.getInt("latest",0)){
                    mN.setVisibility(View.INVISIBLE);
                } else {
                    mN.setVisibility(View.VISIBLE);
                }*/
                mN.setVisibility(View.VISIBLE);
//                mNext = Integer.toString(number+1);

                if(number > 1) {
//                    mPrev = Integer.toString(number-1);
                    mP.setVisibility(View.VISIBLE);
                }else {
                    mP.setVisibility(View.INVISIBLE);
                }

                targetFileName = Integer.toString(number) + ".png";
                Bitmap bMap = BitmapFactory.decodeFile(PATH+targetFileName);
                Log.w("XKCD","File : "+PATH+targetFileName);
                DescrTextView.setText(mPreference.getString("description_" + Integer.toString(number), ""));
                setTitle(mPreference.getString("title_"+Integer.toString(number),"")+" (" + number + ")");
                logoimg.setImageBitmap(bMap);
//                img.setImageBitmap(bMap);
            } else {

                mN.setVisibility(View.VISIBLE);
//                if(mNext.length()!=0) {
//                } else {
//                    mN.setVisibility(View.INVISIBLE);
//                }
                if( number > 1) {
                    mP.setVisibility(View.VISIBLE);
                }else {
                    mP.setVisibility(View.INVISIBLE);
                }

                DescrTextView.setText(mDescription);
                setTitle(mTitle+" (" + num + ")");
                logoimg.setImageBitmap(myBitmap);
//                img.setImageBitmap(myBitmap);
            }

            mAttacher.update();
            findViewById(R.id.content).setVisibility(View.VISIBLE);
//            mPullToRefreshLayout.setRefreshComplete();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
