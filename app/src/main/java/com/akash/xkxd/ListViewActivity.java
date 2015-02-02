package com.akash.xkxd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListViewActivity extends ActionBarActivity {

    String PATH;
    SharedPreferences mPreference;
    File folder ;
    List<String> mFilesList;
    ArrayList<Integer> mFiles;
    final String PREFIX = "title_";
    ListView listView1;
    ArrayAdapter<String> adapter;
    File list[];
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        PATH = Environment.getExternalStorageDirectory()+ "/"+"XKCD";
        folder = new File(PATH);
        list = folder.listFiles();
        mFilesList = new ArrayList<String>();
        mFiles = new ArrayList<Integer>() ;

        setTitle("XKCD Comics Browser");

        mPreference = getSharedPreferences("XKCD_PREF", Context.MODE_PRIVATE);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeLayout.setProgressBackgroundColor(R.color.progress_spinner);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
                intent.putExtra(getPackageName() + ".NUMBER", 0);
                startActivity(intent);
            }
        });

//       try{
//
//           for(File file : list){
//               String name = file.getName().replaceAll("[^0-9]","");
//               if(!mPreference.getString(PREFIX + name, "").equals("")) {
//                   mFiles.add(Integer.parseInt(name));
//               }
//           }
//
//           Collections.sort(mFiles);
//
//           for (Integer num : mFiles){
//               mFilesList.add( num + " - " + mPreference.getString(PREFIX + num, ""));
//           }
//
//       } catch (NullPointerException e){
//           mFilesList.add("noFiles");
//           Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
//           intent.putExtra(getPackageName() + ".NUMBER", 0);
//           startActivity(intent);
//       }


        listView1 = (ListView) findViewById(R.id.listview);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mFilesList);

        listView1.setAdapter(adapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                int num = mFiles.get(position);
                Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
                intent.putExtra(getPackageName() + ".NUMBER", num);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {

        try{
        list = folder.listFiles();
        mFiles.clear();

        for(File file : list){
            String name = file.getName().replaceAll("[^0-9]","");
            if(!mPreference.getString(PREFIX + name, "").equals("")) {
                mFiles.add(Integer.parseInt(name));
            }
        }

        Collections.sort(mFiles);
        mFilesList.clear();

        for (Integer num : mFiles){
            mFilesList.add( num + " - " + mPreference.getString(PREFIX + num, ""));
        }
    } catch (NullPointerException e){
        mFilesList.add("noFiles");
        Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
        intent.putExtra(getPackageName() + ".NUMBER", 0);
        startActivity(intent);
    }


    adapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(false);
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

            case R.id.action_whatif:
                Intent intent = new Intent(this, WhatIf.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
