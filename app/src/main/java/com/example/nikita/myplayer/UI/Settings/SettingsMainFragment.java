package com.example.nikita.myplayer.UI.Settings;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.BuildConfig;
import com.example.nikita.myplayer.R;


/**
 * Created by nikita on 27.08.17.
 */

public class SettingsMainFragment extends Fragment {
    private static final String TAG = "SettingsMainFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_main, container, false);
        initUI(view);

        return view;
    }

    private void initUI(View view) {

        //import
        View itemImport = view.findViewById(R.id.fsm_import);
        itemImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SettingsActivity) getActivity()).showImport();
            }
        });

        // about / get app version
        String versionNum = BuildConfig.VERSION_NAME;
        String version = getString(R.string.fsm_about_desc);
        version = String.format(version, versionNum);
        TextView textAboutVersion = (TextView) view.findViewById(R.id.fsm_about_desc);
        textAboutVersion.setText(version);


    }

}
