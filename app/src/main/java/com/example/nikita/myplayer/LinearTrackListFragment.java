package com.example.nikita.myplayer;


import android.app.Activity;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class LinearTrackListFragment extends Fragment {

    private String TAG = "LinearTrackListFragment";
    private String path;
    private final int FILE_CHOOSER_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Разрешения:  MediaPlayer требует (но молчит) запросить разрешения, иначе выкидывает IOException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1); //request code заменить, естественно
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_linear_track_list, container, false);

        TextView textPressMe = (TextView) view.findViewById(R.id.press_me);
        textPressMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //сразу запускает FileManager
                Intent intent = new Intent(getActivity(), FileManagerActivity.class);
                startActivity(intent);


//                showChooser();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult, resultCode: " + requestCode);

        switch (requestCode) {
            case FILE_CHOOSER_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    path = uri.getPath();
                    Log.i(TAG, "File path: " + path);
                    startPlayerActivity(path);
                }
                break;
            }

            default:
                break;
        }
    }

    //запускает PlayerActivity с указанным путем до файла
    private void startPlayerActivity(String path){
        Intent intent = PlayerActivity.newIntent(getContext(), path);
        startActivity(intent);
    }

    /*
    Ниже кривая реализаця файлового менеджера,
    но она работает (-_-)
    */
    private void showChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_CHOOSER_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getActivity(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }


    }
}
