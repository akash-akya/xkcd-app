package com.akash.xkcd;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.akash.xkcd.util.XkcdData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by akash on 11/11/17.
 */

class ComicsPageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = ComicsPageAdapter.class.getSimpleName();
    private final FragmentManager mFragmentManager;
    private int maxNumber;
    private List<XkcdData> mComics;

    private Comparator<XkcdData> comparator = new Comparator<XkcdData>() {
        @Override
        public int compare(XkcdData o1, XkcdData o2) {
            if(o1.getNum()==o2.getNum())
                return 0;
            else if(o1.getNum()>o2.getNum())
                return 1;
            else
                return -1;
        }
    };

    ComicsPageAdapter(FragmentManager fragmentManager, ArrayList<XkcdData> comics) {
        super(fragmentManager);
        this.mFragmentManager = fragmentManager;
        this.mComics = comics;
        try {
            maxNumber = Collections.max(mComics, comparator).getNum();
        } catch (NoSuchElementException e){
            e.printStackTrace();
            maxNumber = 0;
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment page = mFragmentManager.findFragmentByTag("android:switcher:" + R.id.comics_view_pager
                + ":" + position);

        if (page != null) {
            Log.d(TAG, "getItem: page exists :"+position);
            return page;
        }
        return ImageFragment.init(position);
                //ImageFragment.init(sDbHelper, position, sActionBar, ComicsActivity.this, mSwipeRandom);
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return  (mComics == null) ? 0 : maxNumber+1;
    }

    void UpdateComics(ArrayList<XkcdData> comics) {
        mComics.addAll(comics.subList(mComics.size(), comics.size()));
        maxNumber = Collections.max(mComics, comparator).getNum();
    }
}

