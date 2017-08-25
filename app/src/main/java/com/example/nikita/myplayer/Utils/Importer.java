package com.example.nikita.myplayer.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.LoginFilter;
import android.util.Log;

import com.example.nikita.myplayer.Database.TrackDataBase;
import com.example.nikita.myplayer.Model.Track;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by nikita on 22.08.17.
 */

public class Importer {
    private static final String TAG = "Importer";
    private static Thread mImportThread;

    private Importer(){}

    //импортирует треки с диска в бд. Если withInner = true - включая вложенные папки
    public static void doImport(final String path, final boolean withInner, final Context context){
        final long time = System.nanoTime(); //замер времени выполнения

        mImportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File importFile = new File(path);

                Log.d(TAG, "Import... ");
                if(importFile.isDirectory()){ //импорт из директории
                    List<File> files = StorageHelper.getTracks(new File(path), withInner);

                    if (files == null || files.isEmpty()){
                        Log.d(TAG, "tracks not found\nImport time: " + (System.nanoTime() - time) / 1000000 + "ms");
                        // throw new RuntimeException("track list is empty"); предупреди пользователя
                        return;
                    }

                    //добавляем только не добавленные
                    List<File> savedFile = TrackDataBase.getAllAsFiles(context);
                    if(savedFile != null){
                        if(files.removeAll(savedFile))
                            Log.d(TAG, "some files remove, import size = " + files.size());
                    }

                    Track[] tracks = new Track[files.size()];

                    for (int i = 0; i < files.size(); i++){
                        tracks[i] = Track.makeTrack(files.get(i));
                    }

                    TrackDataBase.putTracks(context, tracks);

                } else { //импорт конкретного трека
                    //добавляем только не добавленные
                    List<File> savedFile = TrackDataBase.getAllAsFiles(context);
                    if(savedFile != null && savedFile.contains(importFile)){
                        Log.d(TAG, "This file already imported");
                        return;
                    }
                    TrackDataBase.putTracks(context, new Track[]{Track.makeTrack(importFile)});
                }

                Log.d(TAG, "Import time: " + (System.nanoTime() - time) / 1000000 + "ms");
            }
        });

        mImportThread.start();

    }

}
