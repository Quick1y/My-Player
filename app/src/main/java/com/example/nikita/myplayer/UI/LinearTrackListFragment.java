package com.example.nikita.myplayer.UI;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.R;


public class LinearTrackListFragment extends Fragment {

    private String TAG = "LinearTrackListFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_linear_track_list, container, false);

        TextView textPressMe = (TextView) view.findViewById(R.id.press_me);
        textPressMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //сразу запускает FileManager
                Intent intent = new Intent(getActivity(), FileManagerActivity.class);
                startActivityForResult(intent, FileManagerActivity.GET_PATH);

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult, resultCode: " + requestCode);

        switch (requestCode) {
            case FileManagerActivity.GET_PATH: {
                if (resultCode == FileManagerActivity.GET_PATH_OK) {
                    String path = data.getStringExtra(FileManagerActivity.KEY_STRING);
                    boolean withInner = data.getBooleanExtra(FileManagerActivity.KEY_WITH_INNER, false);

                    Toast.makeText(getContext(), "Path = " + path + "; with inner = " + withInner, Toast.LENGTH_LONG).show();
                }
                break;
            }

            default:
                break;
        }
    }





}
