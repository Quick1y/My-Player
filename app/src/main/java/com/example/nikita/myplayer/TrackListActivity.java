package com.example.nikita.myplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TrackListActivity extends AppCompatActivity {

    private String TAG = "TrackListActivity";
    private String path;
    private final int FILE_CHOOSER_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        // Разрешения:  MediaPlayer требует (но молчит) запросить разрешения, иначе выкидывает IOException
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1); //request code заменить, естественно
        }

        TextView textPressMe = (TextView) findViewById(R.id.press_me);
        textPressMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        Intent intent = PlayerActivity.newIntent(this, path);
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
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
