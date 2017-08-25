package com.example.nikita.myplayer.Model;

import android.graphics.drawable.Drawable;

import com.example.nikita.myplayer.Utils.FileQualifier;

import java.io.File;

/**
 * Created by nikita on 22.08.17.
 */

public class Track {
    private static final String TAG = "Track";

    private int mId;
    private String mName;
    private String mArtist;
    private String mPath;
    private Drawable mAlbumImg;
    private String mAlbumImgPath;
    private int mCurrentTime;
    private int mDuration;

    //id генерируется в бд
    public Track(int id, String name, String artist, String path, String imgPath, int currTime, int duration){
        mId = id;
        mName = name;
        mArtist = artist;
        mPath = path;
        if(imgPath != null){
            mAlbumImg = Drawable.createFromPath(imgPath);
            mAlbumImgPath = imgPath;
        }
        mCurrentTime = currTime;
        mDuration = duration;
    }

    public Track(String name, String artist, String path, String imgPath, int duration){
        mId = 0;
        mName = name;
        mArtist = artist;
        mPath = path;
        if(imgPath != null){
            mAlbumImg = Drawable.createFromPath(imgPath);
            mAlbumImgPath = imgPath;
        }

        mDuration = duration;

        mCurrentTime = 0;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getPath() {
        return mPath;
    }

    public Drawable getAlbumImg() {
        return mAlbumImg;
    }

    public String getAlbumImgPath(){ return mAlbumImgPath; }

    public int getCurrentTime() {
        return mCurrentTime;
    }

    public int getDuration(){ return mDuration; }


    //создает трек из файла
    public static Track makeTrack(File file){
        if(!FileQualifier.isTrack(file))
            throw new IllegalArgumentException("File is not track");

        String name = file.getName();
        String artist = file.getParentFile().getName();
        String path = file.getPath();
        String imgPath = null;
        int duration = 0;

        return new Track(name, artist, path, imgPath, duration);
    }
}
