package com.example.nikita.myplayer.Database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.nikita.myplayer.Model.Track;

import java.io.File;

/**
 * Created by nikita on 14.07.17.
 */

public class MyCursorWrapper extends CursorWrapper {
    public MyCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Track getTrack() {
        int id = getInt(getColumnIndex("_id"));
        String name = getString(getColumnIndex(DatabaseTable.Column.TRACK_NAME));
        String artist = getString(getColumnIndex(DatabaseTable.Column.TRACK_ARTIST));
        String path = getString(getColumnIndex(DatabaseTable.Column.TRACK_PATH));
        String imgPath = getString(getColumnIndex(DatabaseTable.Column.TRACK_ALBUM_IMG));
        int currTime = getInt(getColumnIndex(DatabaseTable.Column.TRACK_CURR_TIME));
        int duration = getInt(getColumnIndex(DatabaseTable.Column.TRACK_DURATION));

        return new Track(id, name, artist, path, imgPath, currTime, duration);
    }

    public File getFile() {
        String path = getString(getColumnIndex(DatabaseTable.Column.TRACK_PATH));

        File file = new File(path);

        if(file.isFile()){
            return file;
        } else {
            return null;
        }
    }



}
