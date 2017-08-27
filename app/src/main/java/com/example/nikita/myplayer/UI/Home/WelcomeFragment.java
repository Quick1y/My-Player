package com.example.nikita.myplayer.UI.Home;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.UI.FileManagerActivity;
import com.example.nikita.myplayer.UI.Settings.SettingsActivity;
import com.example.nikita.myplayer.Utils.Importer;
import com.example.nikita.myplayer.Utils.MySpinnerAdapter;

public class WelcomeFragment extends Fragment {
    private static final String TAG = "WelcomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_welcome, container, false);

        Button importButton = (Button) view.findViewById(R.id.wf_import_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = FileManagerActivity.getIntent(getActivity());
                startActivityForResult(intent, FileManagerActivity.GET_PATH);
            }
        });



        //ActionBar menu
        final String[] data = getResources().getStringArray(R.array.ha_menu_item);

        MySpinnerAdapter adapter = new MySpinnerAdapter(data, getActivity().getLayoutInflater());
        final Spinner spinner = (Spinner) view.findViewById(R.id.ab_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstStart = true;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!firstStart){ // ignore first click (from system)

                    switch (i) {
                        case 0:
                            Intent intent = SettingsActivity.getIntent(getActivity());
                            startActivity(intent);
                    }

                } else {
                    firstStart = false;
                }
                ((TextView) view).setText("");
                spinner.setSelection(data.length-1); // это костыль, чтобы спиннер работал как меню.
                // Подробнее в getCount() MySpinnerAdapter
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // /ActionBar menu





        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult, resultCode: " + requestCode);

        switch (requestCode) {
            case FileManagerActivity.GET_PATH: {
                if (resultCode == FileManagerActivity.GET_PATH_DIR) { //импорт директории
                    String path = data.getStringExtra(FileManagerActivity.KEY_STRING);
                    boolean withInner = data.getBooleanExtra(FileManagerActivity.KEY_WITH_INNER, false);

                    Importer.doImport(path, withInner, getActivity());

                    Toast.makeText(getActivity(), "Path = " + path + "; with inner = " + withInner, Toast.LENGTH_LONG).show();
                }

                if (resultCode == FileManagerActivity.GET_PATH_FILE){//импорт конкретного трека
                    String path = data.getStringExtra(FileManagerActivity.KEY_STRING);

                    Importer.doImport(path, false, getActivity());

                    Toast.makeText(getActivity(), "Single file. Path = " + path, Toast.LENGTH_LONG).show();

                }
                break;
            }

            default:
                break;
        }
    }


}
