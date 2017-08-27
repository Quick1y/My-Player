package com.example.nikita.myplayer.UI.Settings;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.UI.Home.PlayerFragment;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private FragmentManager mFragmentManager;


    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mFragmentManager = getFragmentManager();
        showMain();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.settings_activity_fragment_container);
        if (fragment instanceof SettingsMainFragment) {
            super.onBackPressed();
        } else {
            showMain();
        }
    }


    //Запускает главный экран настроек
    public void showMain(){
        Fragment fragment = mFragmentManager.findFragmentById(R.id.settings_activity_fragment_container);

        if (fragment != null) {
            fragment = new SettingsMainFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.settings_activity_fragment_container, fragment)
                    .commit();
        } else {
            fragment = new SettingsMainFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.settings_activity_fragment_container, fragment)
                    .commit();
        }

    }

    //Запускает экран импорта
    public void showImport(){
        Fragment fragment = mFragmentManager.findFragmentById(R.id.settings_activity_fragment_container);

        if (fragment != null) {
            fragment = new SettingsImportFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.settings_activity_fragment_container, fragment)
                    .commit();
        } else {
            fragment = new SettingsImportFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.settings_activity_fragment_container, fragment)
                    .commit();
        }

    }


}
