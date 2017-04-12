package com.akash.xkxd.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_XKCD = "XKCD";
    private static final String KEY_NUM = "num";
    private static final String KEY_DAY = "day";
    private static final String KEY_MONTH = "month";
    private static final String KEY_YEAR = "year";
    private static final String KEY_ALT = "alt";
    private static final String KEY_TITLE = "title";
    private static final String KEY_IMG = "img";
    private static final String KEY_FAVORITE = "favorite";
    private static final String TAG = "DataBaseHelper";

    private static String DB_PATH;

    private static String DB_NAME = "myDBName";

    private SQLiteDatabase myDataBase;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        DB_PATH = "/data/data/"+context.getPackageName()+"/databases/";
    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
//            Log.d(TAG, "createDataBase: Database Exists");
        }else{
            this.getReadableDatabase();
        }

    }

    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }catch(SQLiteException e){
            e.printStackTrace();
        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null;
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + DATABASE_XKCD + " (" +
                KEY_NUM + " INTEGER PRIMARY KEY, " +
                KEY_DAY + " TEXT NOT NULL, " +
                KEY_MONTH + " TEXT NOT NULL, " +
                KEY_YEAR + " TEXT NOT NULL, " +
                KEY_TITLE + " TEXT, " +
                KEY_ALT + " TEXT, " +
                KEY_IMG + " TEXT, " +
                KEY_FAVORITE + " INTEGER);"
        );
    }

    public long insertXkcd(XkcdData xkcd) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NUM, xkcd.getNum());
        cv.put(KEY_DAY, xkcd.getDay());
        cv.put(KEY_MONTH, xkcd.getMonth());
        cv.put(KEY_YEAR, xkcd.getYear());
        cv.put(KEY_TITLE, xkcd.getTitle());
        cv.put(KEY_ALT, xkcd.getAlt());
        cv.put(KEY_IMG, xkcd.getImg());
        cv.put(KEY_FAVORITE, xkcd.getFavorite());
        return myDataBase.insert(DATABASE_XKCD, null, cv);
    }

    public ArrayList<XkcdData> getAllComics(){
        ArrayList<XkcdData> comics = new ArrayList<>();
        String selectQuery = "SELECT " +
                KEY_NUM + ", " +
                KEY_DAY + ", " +
                KEY_MONTH + ", " +
                KEY_YEAR + ", " +
                KEY_TITLE + ", " +
                KEY_ALT + ", " +
                KEY_IMG + ", " +
                KEY_FAVORITE +
                " FROM " + DATABASE_XKCD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int num = Integer.parseInt(cursor.getString(0));
                String day = cursor.getString(1);
                String month = cursor.getString(2);
                String year = cursor.getString(3);
                String title = cursor.getString(4);
                String alt = cursor.getString(5);
                String img = cursor.getString(6);
                int favorite = cursor.getInt(7);

                XkcdData xkcdData = new XkcdData(num, day, month, year,
                        title, alt, img, favorite);

                comics.add(xkcdData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return comics;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_XKCD);
        onCreate(db);
    }

    public XkcdData getComic(int num) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DATABASE_XKCD, new String[] { KEY_NUM,  KEY_DAY,  KEY_MONTH,
                KEY_YEAR, KEY_TITLE, KEY_ALT,  KEY_IMG, KEY_FAVORITE},
                KEY_NUM + "=?",
                new String[] { String.valueOf(num) }, null, null, null, null);

        XkcdData comic = null;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            String day = cursor.getString(1);
            String month = cursor.getString(2);
            String year = cursor.getString(3);
            String title = cursor.getString(4);
            String alt = cursor.getString(5);
            String img = cursor.getString(6);
            int favorite = cursor.getInt(7);

            comic = new XkcdData(num, day, month, year, title, alt, img, favorite);
            cursor.close();
        }
        db.close();
        return comic;
    }

    public int getMaxNumber() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT Max("+ KEY_NUM +") FROM "+DATABASE_XKCD;
        Cursor cursor;
        int num = 0;

        try {
            cursor = db.rawQuery(sql,null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                num = cursor.getInt(0);
                cursor.close();
            }
        } catch (SQLException mSQLException) {
            mSQLException.printStackTrace();
        }

        db.close();
        return num;
    }

    public long setFavorite(int num, boolean state) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_FAVORITE, state? 1 : 0);
        return myDataBase.update(DATABASE_XKCD, cv, KEY_NUM+"="+num, null);
    }

    public ArrayList<XkcdData> getFavoriteComics() {
        ArrayList<XkcdData> comics = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DATABASE_XKCD, new String[] { KEY_NUM,  KEY_DAY,  KEY_MONTH,
                        KEY_YEAR, KEY_TITLE, KEY_ALT,  KEY_IMG, KEY_FAVORITE},
                KEY_FAVORITE + "=?",
                new String[] { String.valueOf(1) }, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int num = Integer.parseInt(cursor.getString(0));
                String day = cursor.getString(1);
                String month = cursor.getString(2);
                String year = cursor.getString(3);
                String title = cursor.getString(4);
                String alt = cursor.getString(5);
                String img = cursor.getString(6);
                int favorite = cursor.getInt(7);

                XkcdData xkcdData = new XkcdData(num, day, month, year,
                        title, alt, img, favorite);

                comics.add(xkcdData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return comics;
    }
}