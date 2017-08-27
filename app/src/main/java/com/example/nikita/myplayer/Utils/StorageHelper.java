package com.example.nikita.myplayer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ListAdapter;

import com.example.nikita.myplayer.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nikita on 21.08.17.
 */

public class StorageHelper {
    private static final String TAG = "StorageHelper";
    public static final String PREF_SOURCES_SPLIT = ":{withInner}:";

    //Сортирует список файлов по алфавиту, устанавливая сначала папки, потом файлы
    public static ArrayList<File> sortFileList(ArrayList<File> fileList) {
        int lastDirIndex = 0;

        if (fileList == null) {
            return null;
        }

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareToIgnoreCase(t1.getName());
            }
        });

        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).isDirectory()) {
                fileList.add(lastDirIndex++, fileList.get(i));
                fileList.remove(i + 1);
            }
        }
        return fileList;
    }


    // возвращает список файлов по заданному адресу. Если withEmpty = false, то пустые папки не добаляются
    public static Set<File> getSetFiles(File dir, boolean withEmpty) {
        if (dir == null || dir.listFiles() == null) {
            Log.d(TAG, "Empty directory");
            return null;
        }

        Set<File> files = new HashSet<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                if (withEmpty) {
                    files.add(file);
                } else {
                    if (file.listFiles() != null) {
                        files.add(file);
                    }
                }
            } else
                files.add(file);
        }

        return files;
    }


    //Возвращает список файлов по указанному адресу, являющихся треками.
    //Если withInner = true, то включает и треки из вложенных дирректорий
    public static List<File> getTracks(File dir, boolean withInner) {
        if (dir == null || dir.listFiles() == null) {
            Log.d(TAG, "Empty directory");
            return null;
        }

        //если withInner = true, то рекурсивно ходит по всем директриям и добавляет треки в tracks
        //если false, то просто добавляет треки их указанной директории
        List<File> tracks = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory() && withInner) {
                List<File> innerTracks = getTracks(file, true);
                if(innerTracks != null){
                    tracks.addAll(innerTracks);
                }
            } else {
                if(FileQualifier.isTrack(file)){
                    tracks.add(file);
                }
            }
        }

        return tracks;
    }

}
