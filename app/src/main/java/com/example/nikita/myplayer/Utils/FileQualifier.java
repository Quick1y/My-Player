package com.example.nikita.myplayer.Utils;

import android.util.Log;

import com.example.nikita.myplayer.Model.TrackExtension;

import java.io.File;

/**
 * Created by nikita on 11.08.17.
 */

public class FileQualifier {
    private static final String TAG = "FileQualifier";

    private FileQualifier(){}

    public static boolean isTrack(File file){
        String ext = file.getName();
        char[] extArr = ext.toCharArray();
        for (int i = ext.length() - 1; i > 0; i--){
            if(extArr[i] == '.'){
                ext = ext.substring(i);
             //   Log.d(TAG, "Extension: " + ext);
                break;
            }
        }

        for (String str : TrackExtension.EXTENSIONS){
            if(ext.equals(str)) return true;
        }
        return false;
    }
}
