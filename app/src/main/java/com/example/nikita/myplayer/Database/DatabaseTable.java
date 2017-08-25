package com.example.nikita.myplayer.Database;


/**
 * Created by nikita on 14.07.17.
 */

public class DatabaseTable {
    public static final String TRACKS = "Tracks";

    public class Column{
        public static final String  TRACK_NAME = "name";
        public static final String  TRACK_ARTIST = "artist";
        public static final String  TRACK_PATH = "path";
        public static final String  TRACK_ALBUM_IMG = "album_img";
        public static final String  TRACK_CURR_TIME = "curr_time";
        public static final String  TRACK_DURATION = "duration";
    }

}
