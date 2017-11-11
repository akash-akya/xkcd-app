package com.akash.xkcd.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by akash on 11/11/17.
 */

@Database(entities = {Xkcd.class}, version = 1)
public abstract class ComicsDb extends RoomDatabase {
    private static ComicsDb INSTACE;
    public abstract XkcdDao xkcdDao();

    public static ComicsDb getComicsDb(Context context) {
        if (INSTACE == null) {
            INSTACE = Room.databaseBuilder(context.getApplicationContext(), ComicsDb.class,
                    "xkcd-comics").allowMainThreadQueries().build();
        }
        return INSTACE;
    }

    public static void destroyInstance() {
        INSTACE = null;
    }
}
