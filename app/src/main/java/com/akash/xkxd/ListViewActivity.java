package com.akash.xkxd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
//        PATH = Environment.getExternalStorageDirectory()+ "/"+"XKCD";
//        File folder = new File(PATH);
//        File list[] = folder.listFiles();
//        mFilesList = new ArrayList<String>();
//        mFiles = new ArrayList<Integer>() ;
//
//        mPreference = getSharedPreferences("XKCD_PREF", Context.MODE_PRIVATE);
//
//        for(File file : list){
//            String name = file.getName().replaceAll("[^0-9]","");
//            if(!mPreference.getString(PREFIX + name, "").equals("")) {
//                mFiles.add(Integer.parseInt(name));
//            }
//        }
//
//        Collections.sort(mFiles);
//
//        for (Integer num : mFiles){
//            mFilesList.add( num + " - " + mPreference.getString(PREFIX + num, ""));
//        }
//
//        listView1 = (ListView) findViewById(R.id.listview);
//
//        adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, mFilesList);
//
//        listView1.setAdapter(adapter);
//
//        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
//                int num = mFiles.get(position);
//                Intent intent = new Intent(ListViewActivity.this, MainActivity.class);
//                intent.putExtra(getPackageName() + ".NUMBER", num);
//                startActivity(intent);
//            }
//        });


    }

    @Override
    protected void onResume() {
        PATH = Environment.getExternalStorageDirectory()+ "/"+"XKCD";
        folder = new File(PATH);
        File list[] = folder.listFiles();
        mFilesList = new ArrayList<>();
        mFiles = new ArrayList<>() ;

        mPreference = getSharedPreferences("XKCD_PREF", Context.MODE_PRIVATE);

        for(File file : list){
            String name = file.getName().replaceAll("[^0-9]","");
            if(!mPreference.getString(PREFIX + name, "").equals("")) {
                mFiles.add(Integer.parseInt(name));
            }
        }

        Collections.sort(mFiles);

        for (Integer num : mFiles){
            mFilesList.add( num + " - " + mPreference.getString(PREFIX + num, ""));
        }

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
        super.onResume();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
