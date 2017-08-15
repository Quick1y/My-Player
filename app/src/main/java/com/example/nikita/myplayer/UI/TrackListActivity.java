package com.example.nikita.myplayer.UI;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.UI.LinearTrackListFragment;

public class TrackListActivity extends AppCompatActivity {

    private String TAG = "TrackListActivity";
    private final int PERM_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        //Установка темы
        setTheme(R.style.CommonTheme_orange);

        //просим разрешения и показываем фрагмент
        if(!showPermissionRequest()){
            showFragment();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // тут НЕ вызываем super().onSaveInstanceState(bundle), потому что если вызвать,
        // то словим баг на добавлении фрагмента после запроса пермишенов
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERM_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.tl_need_permission, Toast.LENGTH_LONG).show();
            } else {
                showFragment();
            }
        }
    }


    //true, если разрешения нужны и будут запрошены
    private boolean showPermissionRequest() {
        int permission_write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission_read != PackageManager.PERMISSION_GRANTED
                || permission_write != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"}, PERM_CODE);
                return true;
            } else {
                Toast.makeText(this, R.string.tl_need_permission, Toast.LENGTH_LONG).show();
                return true;
            }
        } else {
            return false;
        }
    }

    private void showFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame_list_act_track_list);

        //Запускает фрагмент с линейным списком
        if (fragment == null) {
            fragment = new LinearTrackListFragment();
            fm.beginTransaction()
                    .add(R.id.frame_list_act_track_list, fragment)
                    .commit();
        }
    }
}
