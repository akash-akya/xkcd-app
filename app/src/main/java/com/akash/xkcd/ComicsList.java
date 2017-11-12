package com.akash.xkcd;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.akash.xkcd.database.ComicsDb;
import com.akash.xkcd.database.Xkcd;
import com.akash.xkcd.util.RequestInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by akash on 11/11/17.
 */

class ComicsList {
    private static final String TAG = ComicsList.class.getName();
    private static final String XKCD_URL = "https://xkcd.com/";
    private final ComicsDb db;
    private final SparseArray<Xkcd> comics;
    private static ComicsList INSTANCE;
    private int maxNumber = 0;

    private ComicsList(Context context){
        db = ComicsDb.getComicsDb(context);
        List<Xkcd> comicsList = db.xkcdDao().getAllComics();
        comics = new SparseArray<>();
        for (Xkcd c : comicsList) {
            if (c.num > maxNumber){
                maxNumber = c.num;
            }
            comics.put(c.num, c);
        }
    }

    static ComicsList getInstance(Context context) {
        if (INSTANCE == null){
            INSTANCE = new ComicsList(context);
        }
        return INSTANCE;
    }

    void getComic(int num, OnGetComicListener onGetComicListener){
        if (comics.get(num) != null) {
            Log.d(TAG, "getComic: exists"+num);
            onGetComicListener.onGetComic(comics.get(num));
        } else {
            Log.d(TAG, "getComic: fetch"+num);
            fetchComic(num, onGetComicListener);
        }
    }

    private boolean isComicExists(int num) {
        return comics.get(num) != null;
    }

    void setComic(Xkcd comic){
        if (comics.get(comic.num) == null){
            Log.d(TAG, "setComic: null");
            throw new NullPointerException();
        }
        comics.put(comic.num, comic);
        db.xkcdDao().updateXkcd(comic);
    }

    private void fetchComic(int num, OnGetComicListener onGetComicListener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(XKCD_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);

        Call<Xkcd> call = service.getComic(num);
        call.enqueue(getOnResponse(onGetComicListener));
    }

    private Callback<Xkcd> getOnResponse(final OnGetComicListener onGetComic){
        return  new Callback<Xkcd>() {
            @Override
            public void onResponse(Call<Xkcd> call, Response<Xkcd> response) {
                if (response.body() != null) {
                    Xkcd comic = response.body();
                    Log.d(TAG, "onResponse: "+comic.num+" exists:"+isComicExists(comic.num));
                    if (!isComicExists(comic.num)){

                        db.xkcdDao().insertXkcd(comic);
                        comics.put(comic.num, comic);
                        if (comic.num > maxNumber){
                            maxNumber = comic.num;
                        }
                    }
                    onGetComic.onGetComic(comic);
                } else {
                    Log.d(TAG, "onResponse: Null!");
                }
            }

            @Override
            public void onFailure(Call<Xkcd> call, Throwable t) {
                Log.d(TAG, "onFailure: "+call);
            }
        };
    }

    int getMaxNumber(){
        return maxNumber;
    }

    void getLatestComic(OnGetComicListener onGetComicListener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(XKCD_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface service = retrofit.create(RequestInterface.class);
        Call<Xkcd> call = service.getLatestComic();
        call.enqueue(getOnResponse(onGetComicListener));
    }

    List<Xkcd> getFavoriteComics() {
        return db.xkcdDao().getFavoriteComics();
    }

    List<Xkcd> getAsList() {
        return db.xkcdDao().getAllComics();
    }



    interface OnGetComicListener {
        void onGetComic(Xkcd comic);
    }
}
