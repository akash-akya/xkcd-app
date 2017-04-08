package com.akash.xkxd;

import com.akash.xkxd.util.XkcdJsonData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Belal on 11/3/2015.
 */
public interface RequestInterface {

    @GET("{num}/info.0.json")
    Call<XkcdJsonData> getComic(@Path("num") int number);

    @GET("info.0.json")
    Call<XkcdJsonData> getLatestComic();

}