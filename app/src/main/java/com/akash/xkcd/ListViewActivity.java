package com.akash.xkcd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.akash.xkcd.database.Xkcd;

import java.text.DateFormat;


public class ListViewActivity extends AppCompatActivity implements ComicsListAdapter.OnItemClickListener {
    private static final String TAG = "ListViewActivity";
    public static final String ARG_FAVORITE = "FAVORITE";
    private ComicsListAdapter mAdapter;
    private boolean mListType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_list);

        setTitle("xkcd");
        mListType = getIntent().getBooleanExtra(ARG_FAVORITE, false);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_comics);
        DateFormat dateFormat = DateFormat.getDateInstance();
        mAdapter = new ComicsListAdapter(dateFormat, this, this, mListType);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Xkcd comic) {
        Intent intent = new Intent(ListViewActivity.this, ComicsActivity.class);
        intent.putExtra(ComicsActivity.ARG_COMIC_NUMBER, comic.num);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
        finish();
    }
}
