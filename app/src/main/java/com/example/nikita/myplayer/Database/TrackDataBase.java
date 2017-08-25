package com.example.nikita.myplayer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nikita.myplayer.Model.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikita on 21.08.17.
 */

public class TrackDataBase {
    private static SQLiteDatabase mDatabase;
    private static final String TAG = "SQLiteDatabase";

    //открывает бд на чтение/запись
    private static void init(Context context) {
        if (mDatabase == null) {
            mDatabase = new DatabaseHelper(context).getWritableDatabase();
        }
    }

    @Nullable
    public static Track getTrackById(Context context, int id) {
        init(context);

        //Делаем запрос к бд
        Cursor cursor;
        try {
            cursor = mDatabase.query(
                    DatabaseTable.TRACKS,
                    null,
                    "_id=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        Log.d(TAG, "getTrackById(): cursor size = " + cursor.getCount());
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        //достаем треки
        MyCursorWrapper cursorWrapper = new MyCursorWrapper(cursor);
        Track track;

        try {
            cursorWrapper.moveToFirst();
            track = cursorWrapper.getTrack();
        } finally {
            cursor.close();
            cursorWrapper.close();
        }

        return track;
    }

    public static Track[] getAll(Context context) {
        init(context);

        //Делаем запрос к бд
        Cursor cursor;
        try {
            cursor = mDatabase.query(
                    DatabaseTable.TRACKS,
                    null, null, null,
                    null, null, DatabaseTable.Column.TRACK_NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        Log.d(TAG, "getAll(): cursor size = " + cursor.getCount());
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        //достаем треки
        MyCursorWrapper cursorWrapper = new MyCursorWrapper(cursor);
        Track[] tracks = new Track[cursor.getCount()];
        int i = 0;

        cursorWrapper.moveToFirst();

        //вытаскиваем все треки из wrapper
        try {
            do {
                tracks[i] = cursorWrapper.getTrack();
                i++;
            } while (cursorWrapper.moveToNext());

        } finally {
            cursor.close();
            cursorWrapper.close();
        }

        return tracks;
    }

    public static boolean isEmpty(Context context){
        init(context);

        //Делаем запрос к бд
        Cursor cursor;
        try {
            cursor = mDatabase.query(
                    DatabaseTable.TRACKS,
                    new String[]{"_id"}, //только пути до файлов
                    null, null,
                    null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Query error", ex);
        }

        Log.d(TAG, "isEmpty(): cursor size = " + cursor.getCount());

        if (cursor.getCount() == 0) {
            cursor.close();
            return true;
        }

        return false;
    }


    //возвращает список треков в виде файлов
    public static List<File> getAllAsFiles(Context context) {
        init(context);

        //Делаем запрос к бд
        Cursor cursor;
        try {
            cursor = mDatabase.query(
                    DatabaseTable.TRACKS,
                    new String[]{DatabaseTable.Column.TRACK_PATH}, //только пути до файлов
                    null, null,
                    null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        Log.d(TAG, "cursor size = " + cursor.getCount());
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        //достаем треки
        MyCursorWrapper cursorWrapper = new MyCursorWrapper(cursor);
        List<File> files = new ArrayList<>();
        cursorWrapper.moveToFirst();

        //вытаскиваем все треки из wrapper
        try {
            do {
                files.add(cursorWrapper.getFile());
            } while (cursorWrapper.moveToNext());

        } finally {
            cursor.close();
            cursorWrapper.close();
        }

        return files;
    }


    //добавляет треки в базу данных
    public static void putTracks(Context context, Track[] tracks) {
        init(context);

        ContentValues values = new ContentValues();

        Log.d(TAG, "putTracks(): ");
        for (Track t : tracks) {
            values.put(DatabaseTable.Column.TRACK_NAME, t.getName());
            values.put(DatabaseTable.Column.TRACK_ARTIST, t.getArtist());
            values.put(DatabaseTable.Column.TRACK_ALBUM_IMG, t.getAlbumImg() == null ? null :  t.getAlbumImgPath());
            values.put(DatabaseTable.Column.TRACK_PATH, t.getPath());
            values.put(DatabaseTable.Column.TRACK_CURR_TIME, t.getCurrentTime());

            mDatabase.insert(DatabaseTable.TRACKS, null, values); // добавляем в бд
            Log.d(TAG, "track " + t.getName() + " was imported");
        }

    }
}
