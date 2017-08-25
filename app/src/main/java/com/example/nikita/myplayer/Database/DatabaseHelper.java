package com.example.nikita.myplayer.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nikita on 14.07.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "tracks.db";
    private static final int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Создаем таблицу карт
        sqLiteDatabase.execSQL("CREATE TABLE " + DatabaseTable.TRACKS + "("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseTable.Column.TRACK_NAME + " STRING,  "
                + DatabaseTable.Column.TRACK_ARTIST + " STRING, "
                + DatabaseTable.Column.TRACK_ALBUM_IMG + " STRING, "
                + DatabaseTable.Column.TRACK_PATH + " STRING, "
                + DatabaseTable.Column.TRACK_CURR_TIME + " INTEGER, "
                + DatabaseTable.Column.TRACK_DURATION + " INTEGER)"

        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
