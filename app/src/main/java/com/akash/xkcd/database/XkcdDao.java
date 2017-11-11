package com.akash.xkcd.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by akash on 11/11/17.
 */

@Dao
public interface XkcdDao {

    @Insert
    public void insertXkcd(Xkcd xkcd);

    @Query("SELECT * FROM Xkcd")
    public List<Xkcd> getAllComics();

    @Query("SELECT * FROM Xkcd WHERE favorite=1")
    public List<Xkcd> getFavoriteComics();

    @Query("SELECT * FROM Xkcd WHERE num=:num")
    public Xkcd getComic(int num);

    @Query("SELECT MAX(num) FROM Xkcd")
    public int getMaxNumber();

    @Update
    public int updateXkcd(Xkcd xkcd);
}
