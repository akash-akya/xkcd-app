package com.akash.xkcd.util;

import java.io.Serializable;

public class XkcdData implements Serializable{
    private final String mTranscript;
    private final String mDay;
    private final String mMonth;
    private final String mYear;
    private final int mNum;
    private final String mTitle;
    private final String mAlt;
    private final String mImg;
    private boolean mFavorite;

    public XkcdData(int num, String day, String month, String year, String title,
                    String alt, String img, String transcript,int favorite){
        mDay = day;
        mMonth = month;
        mYear = year;
        mNum = num;
        mTitle = title;
        mAlt = alt;
        mImg = img;
        mTranscript = transcript;
        mFavorite = (favorite != 0);
    }

    public String getTranscript() { return mTranscript; }

    public String getDay() {
        return mDay;
    }

    public String getMonth() {
        return mMonth;
    }

    public String getYear() {
        return mYear;
    }

    public int getNum() {
        return mNum;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAlt() {
        return mAlt;
    }

    public String getImg() {
        return mImg;
    }

    public boolean getFavorite() {
        return  mFavorite;
    }
}
