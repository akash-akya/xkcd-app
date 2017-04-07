package com.akash.xkxd;

import java.io.Serializable;

public class XkcdData implements Serializable{
    private String mDay;
    private String mMonth;
    private String mYear;
    private int mNum;
    private String mTitle;
    private String mAlt;
    private String mImg;

    public XkcdData(int num, String day, String month, String year, String title, String alt, String img){
        mDay = day;
        mMonth = month;
        mYear = year;
        mNum = num;
        mTitle = title;
        mAlt = alt;
        mImg = img;
    }

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
}
