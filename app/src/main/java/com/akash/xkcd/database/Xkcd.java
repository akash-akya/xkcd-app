package com.akash.xkcd.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by akash on 11/11/17.
 */

@Entity
public class Xkcd {
    @PrimaryKey
    public int num;
    public String transcript;
    public String day;
    public String month;
    public String year;
    public String title;
    public String alt;
    public String img;
    public int favorite = 0;

    public Xkcd(int num, String day, String month, String year, String title,
                String alt, String img, String transcript,int favorite){
        this.day = day;
        this.month = month;
        this.year = year;
        this.num = num;
        this.title = title;
        this.alt = alt;
        this.img = img;
        this.transcript = transcript;
        this.favorite = favorite;
    }

    public boolean isFavorite()
    {
        return favorite != 0;
    }

    public void setFavorite(boolean favorite)
    {
        this.favorite = favorite? 1 : 0;
    }
}
