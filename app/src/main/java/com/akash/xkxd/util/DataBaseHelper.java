package com.akash.xkxd.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.akash.xkxd.XkcdData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_XKCD = "XKCD";
    private static final String KEY_NUM = "num";
    private static final String KEY_DAY = "day";
    private static final String KEY_MONTH = "month";
    private static final String KEY_YEAR = "year";
    private static final String KEY_ALT = "alt";
    private static final String KEY_TITLE = "title";
    private static final String KEY_IMG = "img";

    //The Android's default system path of your application database.
    private static String DB_PATH;

    private static String DB_NAME = "myDBName";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
        DB_PATH = "/data/data/"+context.getPackageName()+"/databases/";
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
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
                KEY_IMG + " TEXT);"
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
        return myDataBase.insert(DATABASE_XKCD, null, cv);
    }

    public ArrayList<XkcdData> getAllComics(){
        ArrayList<XkcdData> contactList = new ArrayList<>();
        String selectQuery = "SELECT " +
                KEY_NUM + ", " +
                KEY_DAY + ", " +
                KEY_MONTH + ", " +
                KEY_YEAR + ", " +
                KEY_TITLE + ", " +
                KEY_ALT + ", " +
                KEY_IMG +
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

                XkcdData xkcdData = new XkcdData(num, day, month, year, title, alt, img);

                contactList.add(xkcdData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_XKCD);
        onCreate(db);
    }

    public XkcdData getComic(int num) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DATABASE_XKCD, new String[] { KEY_NUM,  KEY_DAY,  KEY_MONTH,
                KEY_YEAR, KEY_TITLE, KEY_ALT,  KEY_IMG},
                KEY_NUM + "=?",
                new String[] { String.valueOf(num) }, null, null, null, null);

        XkcdData comic = null;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            String day = cursor.getString(1);
            String month = cursor.getString(2);
            String year = cursor.getString(3);
            String title = cursor.getString(5);
            String alt = cursor.getString(4);
            String img = cursor.getString(6);

            comic = new XkcdData(num, day, month, year, title, alt, img);
            cursor.close();
        }
        db.close();
        return comic;
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}