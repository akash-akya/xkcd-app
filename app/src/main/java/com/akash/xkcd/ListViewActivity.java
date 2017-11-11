package com.akash.xkcd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.akash.xkcd.util.XkcdData;

import java.text.DateFormat;
import java.util.ArrayList;


public class ListViewActivity extends AppCompatActivity implements ComicsListAdapter.OnItemClickListener {
    private static final String TAG = "ListViewActivity";
    public static final String ARG_FAVORITE = "FAVORITE";
    private ComicsListAdapter mAdapter;
    private ArrayList<XkcdData> mComics = new ArrayList<>();
    private boolean mListType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comics_list);

        setTitle("xkcd");
        mListType = getIntent().getBooleanExtra(ARG_FAVORITE, false);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_comics);
        DateFormat dateFormat = DateFormat.getDateInstance();
        mAdapter = new ComicsListAdapter(mComics, dateFormat, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<XkcdData> comics = getComicsFromDb(mListType);
        if (comics.size() != mComics.size()) {
            mComics.clear();
            mComics.addAll(comics);
            mAdapter.notifyDataSetChanged();
        }
    }

    public static ArrayList<String> getComicNumbers(ArrayList<XkcdData> comics) {
        ArrayList<String> nums = new ArrayList<>();
        for(XkcdData c : comics) {
            nums.add(String.valueOf(c.getNum()));
        }
        return nums;
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

    public ArrayList<XkcdData> getComicsFromDb(boolean isFavoriteList) {
        return isFavoriteList ? ComicsActivity.sDbHelper.getFavoriteComics() : ComicsActivity.sDbHelper.getAllComics();
    }
}
