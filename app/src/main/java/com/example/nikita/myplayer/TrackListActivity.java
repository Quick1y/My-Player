package com.example.nikita.myplayer;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TrackListActivity extends AppCompatActivity {

    private String TAG = "TrackListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        //Установка темы
        setTheme(R.style.CommonTheme_orange);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame_list_act_track_list);

        //Запускает фрагмент с линейным списком
        if(fragment == null){
            fragment = new LinearTrackListFragment();
            fm.beginTransaction()
                    .add(R.id.frame_list_act_track_list, fragment)
                    .commit();
        }


    }
}
