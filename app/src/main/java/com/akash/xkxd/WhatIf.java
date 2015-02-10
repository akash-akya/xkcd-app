package com.akash.xkxd;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;



public class WhatIf extends ActionBarActivity {

    TextView tv ;
    int num;
    SearchView mSearchView;
    MenuItem mSearchMenuItem;
    String content = "";
    String mTitle;
    int mLatest;
    String mPrev,mNext;
    SwipeRefreshLayout swipeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_if);
        tv = (TextView) findViewById(R.id.whatif_tv);

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        mLatest = 0;

        setTitle("What-If");

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                num=0;
                new GetArtistInfo().execute();

            }
        });

        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeLayout.setProgressBackgroundColor(R.color.progress_spinner);


//        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
//
//        // Now setup the PullToRefreshLayout
//        ActionBarPullToRefresh.from(this)
//                // Mark All Children as pullable
//                .allChildrenArePullable()
//                        // Set a OnRefreshListener
//                .listener( new OnRefreshListener() {
//                    @Override
//                    public void onRefreshStarted(View view) {
//                        num=0;
//                        new GetArtistInfo().execute();
//                    }
//
//
//                })
//        // Finally commit the setup to our PullToRefreshLayout
//        .setup(mPullToRefreshLayout);

        findViewById(R.id.textView2).setVisibility(View.VISIBLE);
        findViewById(R.id.whatifbanner).setVisibility(View.VISIBLE);

        Button mP = (Button) findViewById(R.id.mPrev);
        Button mN = (Button) findViewById(R.id.mNext);

        mNext = "";
        mPrev = "";

        mP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPrev.length() != 0){
                    num = Integer.parseInt(mPrev);
                    new GetArtistInfo().execute();
                }
            }
        });

        mN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNext.length() != 0){
                    num = Integer.parseInt(mNext);
                    new GetArtistInfo().execute();
                }
            }
        });

        mP.setMovementMethod(LinkMovementMethod.getInstance());
        mN.setMovementMethod(LinkMovementMethod.getInstance());

        mP.setVisibility(View.GONE);
        mN.setVisibility(View.GONE);


    }



///////////////////////////// GetArtistInfo AsyncTask . ////////////////////////////
public class GetArtistInfo extends AsyncTask<Void, Void, Void> {

    Document document;
    String mErrorMessage;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        mPullToRefreshLayout.setRefreshing(true);
        swipeLayout.setRefreshing(true);
        mErrorMessage = "";

    }


    @Override
    protected Void doInBackground(Void... params) {
            content = "";
            String iUrl = "http://what-if.xkcd.com/";


            if(num != 0)
            {
                iUrl += num + "/";
            }

            try {
                document = Jsoup.connect(iUrl).get();
            }catch (UnknownHostException e){
                e.printStackTrace();
                mErrorMessage = "Please check your internet connection";
                return null;
            }catch (HttpStatusException e){
                e.printStackTrace();
                mErrorMessage = "Invalid input!";
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }


        Elements description = document
                    .select("article[class=entry]");

            for (Element element : description.select("img"))
                element.remove();

            mTitle = description.select("h1").text();

            for (Element element : description.select("h1"))
                element.remove();

            for (Element element : description.select("span"))
                element.remove();

            content = description.toString();

//
            description = description.select("a");

            mPrev = description.first().attr("href").replaceAll("[^0-9]","");
            if(num == 0)
                mLatest = Integer.parseInt(mPrev);
            mNext = Integer.toString(Integer.parseInt(mPrev)+1);
            mPrev = Integer.toString(Integer.parseInt(mPrev)-1);  //  description.first().attr("href").replaceAll("[^0-9]","");

//            for( Element urlElement : description ) {
//                urlElement.attr("href", "com.akash.xkxd://"+ "whatif" + urlElement.attr("href").replaceAll("[^0-9]",""));
//                Log.w("absURL : ","  -   " +urlElement); // Print result directly after changes have been done
//            }
//
//            if(description.get(0).toString().contains("Prev"))
//                  mPrev = description.first().toString().replaceAll("[^0-9]","");
//
//            if(description.get(0).toString().contains("Prev"))
//                mPrev = description.first().toString().replaceAll("[^0-9]","");
//
//            if(description.get(1).toString().contains("Next"))
//                  mNext = description.get(1).toString().replaceAll("[^0-9]","");

            return null;

    }

    @Override
    protected void onPostExecute(Void result) {

        if(mErrorMessage.length() != 0){
            if(mLatest == num){
                Toast.makeText(getApplicationContext(), "You already have latest content!", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder mMessage = new AlertDialog.Builder(WhatIf.this);
                mMessage.setMessage(mErrorMessage);
                mMessage.setTitle("Error");
                mMessage.show();
            }
        } else {
            findViewById(R.id.textView2).setVisibility(View.GONE);
            findViewById(R.id.whatifbanner).setVisibility(View.GONE);

            Button mP = (Button) findViewById(R.id.mPrev);
            Button mN = (Button) findViewById(R.id.mNext);

            if(mPrev.length()!=0 && Integer.parseInt(mPrev)>1) {
                mP.setVisibility(View.VISIBLE);
            }else {
                mP.setVisibility(View.INVISIBLE);
            }

            if(mNext.length()!=0 && mLatest != num) {
                mN.setVisibility(View.VISIBLE);
            }else {
                mN.setVisibility(View.INVISIBLE);
            }

            tv.setText(Html.fromHtml(content));
            setTitle(mTitle);


        }
//        mPullToRefreshLayout.setRefreshing(false);
//        mPullToRefreshLayout.setRefreshComplete();

        swipeLayout.setRefreshing(false);
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_what_if, menu);
        final Menu mMenu = menu;

        mSearchMenuItem = menu.findItem(R.id.search2);
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
                mMenu.findItem(R.id.search2).collapseActionView();
                try {
                    num = Integer.parseInt(query);
                    new GetArtistInfo().execute();

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
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "What-If : "+mTitle);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
//        return shareIntent;
//        startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), Navigator.REQUEST_SHARE_ACTION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share2:
                doShare();
                return true;
            case R.id.action_settings:
                AboutApp.Show(this);
                return true;
            case R.id.action_xkcd:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
//                AboutApp.Show(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
