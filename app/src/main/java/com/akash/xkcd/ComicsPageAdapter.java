package com.akash.xkcd;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by akash on 11/11/17.
 */

class ComicsPageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = ComicsPageAdapter.class.getSimpleName();
    private int maxNumber;

    ComicsPageAdapter(FragmentManager fragmentManager, int maxNumber) {
        super(fragmentManager);
        this.maxNumber = maxNumber;
    }

    @Override
    public ImageFragment getItem(int position) {
        return ImageFragment.init(position);
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }


    @Override
    public int getCount() {
        return  maxNumber+1;
    }

    int getMaxNumber() {
        return maxNumber;
    }

    void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
}

