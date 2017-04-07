package com.akash.xkxd;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends ActionBarActivity  implements GetXkcd.OnCompletionListener{
    private static final String TAG = "MainActivity";

    SearchView mSearchView;
    MenuItem mSearchMenuItem;
    ImageView mComicImg;
    PhotoViewAttacher mAttacher;
    ActionBar mActionBar;
    private XkcdData mXkcdData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mComicImg = (ImageView) findViewById(R.id.imageView);
        LinearLayout mDescContainer = (LinearLayout) findViewById(R.id.desc_container);

        mActionBar = getSupportActionBar();
        verifyStoragePermissions(this);

        mAttacher = new PhotoViewAttacher(mComicImg);

        mDescContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setVisibility(View.GONE);
                return true;
            }
        });

        mDescContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleView();
            }
        });

        mComicImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getBaseContext());
                dialog.setTitle("fake!");
                dialog.setMessage("fake again!");
                dialog.show();
            }
        });


        Button buttonPrev = (Button) findViewById(R.id.mPrevious);
        Button buttonNext = (Button) findViewById(R.id.mNextButton);


        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetXkcd(mXkcdData.getNum()-1, MainActivity.this, MainActivity.this).execute();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetXkcd(mXkcdData.getNum()+1, MainActivity.this, MainActivity.this).execute();
            }
        });

        buttonPrev.setMovementMethod(LinkMovementMethod.getInstance());
        buttonNext.setMovementMethod(LinkMovementMethod.getInstance());

        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LinearLayout descContainer = (LinearLayout) findViewById(R.id.desc_container);
                if (descContainer != null) {
                    if(descContainer.getVisibility() == View.VISIBLE){
                        descContainer.setVisibility(View.GONE);
                    } else {
                        descContainer.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }
        });

        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                toggleView();
            }
        });

        int num = getIntent().getIntExtra(getPackageName()+".NUMBER",0);
        new GetXkcd(num, this, this).execute();
    }

    void toggleView(){
        if (mActionBar != null) {
            if(mActionBar.isShowing()){
                mActionBar.hide();
                findViewById(R.id.buttons).setVisibility(View.GONE);
                findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff000000"));

            }else {
                mActionBar.show();
                findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.image_background).setBackgroundColor(Color.parseColor("#ff222222"));
            }
        }
    }

/*
    AsyncTask GetXkcdTask  = new GetXkcd(0, getWindow().getDecorView().getRootView(), this) {
        @Override
        protected void onPostExecute(Void result) {

        }
    };
*/

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


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
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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
                 * hides and then un-hides search tab to make sure keyboard disappears when query is submitted
                 */
                mMenu.findItem(R.id.search3).collapseActionView();
                try {
                    int num = Integer.parseInt(query);
                    new GetXkcd(num, MainActivity.this, MainActivity.this).execute();

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.w("akashXkcd",query);
                }
                return true;
            }
        });
        return true;
    }

    public void doShare() {
        // populate the share intent with data
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String mAbsPath =  new File(Util.getFilePath(mXkcdData.getNum())).getAbsolutePath();

        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mXkcdData.getAlt());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mAbsPath)));
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
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

    @Override
    public void onCompletion(XkcdData xkcdData) {
        mXkcdData = xkcdData;

        Button mP = (Button) findViewById(R.id.mPrevious);
        if( xkcdData.getNum() > 1) {
            mP.setVisibility(View.VISIBLE);
        }else {
            mP.setVisibility(View.INVISIBLE);
        }
        TextView DescrTextView = (TextView) findViewById(R.id.description);
        DescrTextView.setText(xkcdData.getTitle());

        Log.d(TAG, "onCompletion: "+xkcdData.getTitle());

        Bitmap bMap = BitmapFactory.decodeFile(Util.getFilePath(xkcdData.getNum()));
        mComicImg.setImageBitmap(bMap);

//        Log.w("XKCD", "File : " + Util.getFilePath(xkcdData.getNum()));

        DescrTextView.setText(mXkcdData.getAlt());
        setTitle(mXkcdData.getTitle() + " (" + xkcdData.getNum() + ")");

        mAttacher.update();

        findViewById(R.id.content).setVisibility(View.VISIBLE);
        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    @Override
    public void onPreStart() {
        findViewById(R.id.content).setVisibility(View.GONE);
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    @Override
    public void OnFail(int status) {
        Log.d(TAG, "OnFail: " + status);
        if (status == GetXkcd.INVALID_NUM){
            AlertDialog.Builder mMessage = new AlertDialog.Builder(MainActivity.this);
            mMessage.setMessage("message");
            mMessage.setTitle("Error");
            mMessage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            mMessage.show();
        }
        /*
        else if (status == GetXkcd.INVALID_NUM) {
            if (num == 0) {
                Toast.makeText(getApplicationContext(), "You already have latest content!", Toast.LENGTH_SHORT).show();
//                    mN.setVisibility(View.INVISIBLE);
            } else if (num >= mPreference.getInt("latest", 0)) {
                mN.setVisibility(View.INVISIBLE);
            } else {
                mN.setVisibility(View.VISIBLE);
            }

            mN.setVisibility(View.VISIBLE);
//                mNext = Integer.toString(number+1);

            if (number > 1) {
//                    mPrev = Integer.toString(number-1);
                mP.setVisibility(View.VISIBLE);
            } else {
                mP.setVisibility(View.INVISIBLE);
            }

            targetFileName = Integer.toString(number) + ".png";

        }
           */
    }
}
