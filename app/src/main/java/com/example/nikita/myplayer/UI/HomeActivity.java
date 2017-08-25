package com.example.nikita.myplayer.UI;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.nikita.myplayer.Database.TrackDataBase;
import com.example.nikita.myplayer.Model.PermissionChecker;
import com.example.nikita.myplayer.Model.Track;
import com.example.nikita.myplayer.R;

public class HomeActivity extends AppCompatActivity {

    private String TAG = "HomeActivity";
    private final int PERM_CODE = 101;

    private FragmentManager mFragmentManager;
    private Window mWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindow = getWindow();
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mFragmentManager = getFragmentManager();

        //просим разрешения и показываем фрагмент
        if (!showPermissionRequest()) {
            if(TrackDataBase.isEmpty(this)){
                showWelcome();
            } else {
                showList();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // тут НЕ вызываем super().onSaveInstanceState(bundle), потому что если вызвать,
        // то словим баг на добавлении фрагмента после запроса пермишенов
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Ответ пришел, " + (requestCode == PERM_CODE) + ", " + (grantResults.length != 0));

        if (requestCode == PERM_CODE && grantResults.length != 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.tl_need_permission, Toast.LENGTH_LONG).show();

            } else {
                if(TrackDataBase.isEmpty(this)){
                    showWelcome();
                } else {
                    showList();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.frame_list_act_track_list);
        if (fragment instanceof PlayerFragment) {
            showList();
        } else {
            super.onBackPressed();
        }

    }


    //true, если разрешения нужны и будут запрошены
    private boolean showPermissionRequest() {

        return PermissionChecker.requestPermission(this);
        /*int permission_write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        }*/
    }

    private void showWelcome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindow.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }

        Fragment fragment = mFragmentManager.findFragmentById(R.id.frame_list_act_track_list);

        //Запускает фрагмент с плеером
        if (fragment != null) {
            fragment = new WelcomeFragment();
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_alpha_show, R.animator.player_close)
                    .replace(R.id.frame_list_act_track_list, fragment)
                    .commit();
        } else {
            fragment = new WelcomeFragment();
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_alpha_show, R.animator.player_close)
                    .add(R.id.frame_list_act_track_list, fragment)
                    .commit();
        }


    }


    public void showList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindow.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }

        Fragment fragment = mFragmentManager.findFragmentById(R.id.frame_list_act_track_list);

        //Запускает фрагмент с плеером
        if (fragment != null) {
            fragment = new LinearTrackListFragment();
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_alpha_show, R.animator.player_close)
                    .replace(R.id.frame_list_act_track_list, fragment)
                    .commit();
        } else {
            fragment = new LinearTrackListFragment();
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_alpha_show, R.animator.player_close)
                    .add(R.id.frame_list_act_track_list, fragment)
                    .commit();
        }

    }


    public void showPlayer(Track track) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindow.setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        Fragment fragment = mFragmentManager.findFragmentById(R.id.frame_list_act_track_list);
        int tracklId;
        if(track == null){
            tracklId = -1;
        } else {
            tracklId = track.getId();
        }

        //Запускает фрагмент с плеером
        if (fragment != null) {
            fragment = PlayerFragment.newInstance(tracklId);
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.player_open, R.animator.fragment_alpha_hide)
                    .replace(R.id.frame_list_act_track_list, fragment)
                    .commit();
            Log.d(TAG, "fragment is not null: " + fragment.getClass());
        } else {
            fragment = PlayerFragment.newInstance(tracklId);
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.player_open, R.animator.fragment_alpha_hide)
                    .add(R.id.frame_list_act_track_list, fragment)
                    .commit();
            Log.d(TAG, "fragment is null");
        }
    }
}
