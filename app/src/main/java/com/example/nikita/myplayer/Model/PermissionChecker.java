package com.example.nikita.myplayer.Model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.nikita.myplayer.R;

/**
 * Created by nikita on 20.08.17.
 */

public class PermissionChecker{
    private static final String TAG = "PermissionChecker";

    private final static int PERM_CODE = 101;

    public static boolean requestPermission(Activity context){
        int permission_write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);


        if (permission_read != PackageManager.PERMISSION_GRANTED
                || permission_write != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"}, PERM_CODE);
                return true;
            } else {
                Toast.makeText(context, R.string.tl_need_permission, Toast.LENGTH_LONG).show();
                return true;
            }
        } else {
            return false;
        }
    }

}
