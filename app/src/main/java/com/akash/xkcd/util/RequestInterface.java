package com.akash.xkcd.util;

import com.akash.xkcd.database.Xkcd;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RequestInterface {
    @GET("{num}/info.0.json")
    Call<Xkcd> getComic(@Path("num") int number);

    @GET("info.0.json")
    Call<Xkcd> getLatestComic();
}