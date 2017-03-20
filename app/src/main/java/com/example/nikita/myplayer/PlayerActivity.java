package com.example.nikita.myplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {
    private final static String TAG = "PlayerActivity";
    private final int FILE_CHOOSER_CODE = 1;

    private ImageButton playButton;
    private ImageButton pauseButton;
    private TextView textViewPath;
    private SeekBar timeBar;
    private TextView textViewCurrTime;
    private TextView textViewDurTime;

    private String audioFilePath = "/storage/7F2A-1905/Music/am_oyb.mp3"; //это временно
    private ProgressChangeTask progressTask; //AsyncTask, отслеживающий прогресс трека
    private boolean canUpdateTimeBar = true; // false при перетаскивании ползунка

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Разрешения:  MediaPlayer требует (но молчит) запросить разрешения, иначе выкидывает IOException
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1); //request code заменить, естественно
        }

        // Создание плеера:  плеер нужно создавать сразу, и, возможно, в AsyncTask  !!!
        try {
            AudioPlayer.create(audioFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
        }


        // Путь к файлу:  Устанавливает в TextView путь к файлу
        textViewPath = (TextView) findViewById(R.id.activity_player_path_text);
        String textAudioPath = "Path: " + audioFilePath;
        textViewPath.setText(textAudioPath);
        //небольшой эксперимент с файловым менеджером
        textViewPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressTask != null) {
                    progressTask.stop();
                }
                AudioPlayer.destroy();
                showChooser();
            }
        });

        // Текстовое поле время:  текущее и общее время трека
        textViewCurrTime = (TextView) findViewById(R.id.activity_player_currtime_text);
        textViewDurTime = (TextView) findViewById(R.id.activity_player_durtime_text);
        if(AudioPlayer.isCreated()){
            textViewDurTime.setText(millisecToTime(AudioPlayer.getDuration()));
        } else {
            textViewDurTime.setText(millisecToTime(0));
        }


        playButton = (ImageButton) findViewById(R.id.activity_player_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayClicked();
            }
        });

        pauseButton = (ImageButton) findViewById(R.id.activity_player_stop_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseClicked();
            }
        });

        //SeekBar: инициализируется и устанавливается в 0
        timeBar = (SeekBar) findViewById(R.id.activity_player_seekBar);
        timeBar.setMax(1000);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                canUpdateTimeBar = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (AudioPlayer.isCreated()) {
                    canUpdateTimeBar = true;
                    AudioPlayer.setProgress(seekBar.getProgress());
                    updateTimeText();
                }
            }
        });

        updateTimeText();
        updateTimeBarProgress();
    }


    //вызывается при клике на Кнопку Плей
    private void onPlayClicked() {
        /* Если плеер не создан, то создает и запускает его.
        Если создан, то просто продолжает воспроизведение.
         */

        if (!AudioPlayer.isCreated()) {
            try {
                String path = audioFilePath; //путь к аудиофайлу
                AudioPlayer.create(path);
                AudioPlayer.play();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Toast.makeText(this, "Невозможно воспроизвести файл", Toast.LENGTH_LONG).show();
            } catch (PlayerNotCreateException pncEx) {
                pncEx.printStackTrace();
            }
        } else {
            try {
                AudioPlayer.play();
            } catch (PlayerNotCreateException pncEx) {
                pncEx.printStackTrace();
            }
        }

        //обновляет общую длину трека
        textViewDurTime.setText(millisecToTime(AudioPlayer.getDuration()));

        //Создание AsyncTask обновляющего timeBar и textViewCurrTime
        createProgressTask();
        progressTask.execute(AudioPlayer.getPlayer());
    }

    //вызывается при клике на Кнопку Стоп
    private void onPauseClicked() {
        try {
            AudioPlayer.pause();
        } catch (PlayerNotCreateException e) {
            e.printStackTrace();
        }
    }


    private void createProgressTask() {
        progressTask = new ProgressChangeTask();

        progressTask.setOnProgressChangeListener(new ProgressChangeTask.OnProgressChangeListener() {
            @Override
            public void onProgressChange() {

                updateTimeBarProgress(); // обновить прогресс для SeekBar timeBar
                updateTimeText(); // обновить текущее время для TextView textViewCurrTime

            }
        });
    }

    //Устанавливает текущее время в TextView textViewCurrTime
    public void updateTimeText() {
        if (AudioPlayer.isCreated()) {
            String time = millisecToTime(AudioPlayer.getCurrentPosition());
            textViewCurrTime.setText(time);
            //Log.i(TAG, "Time: " + time);

        } else {
            textViewCurrTime.setText(millisecToTime(0));
        }

    }

    //Устанавливает прогресс в SeekBar timeBar
    public void updateTimeBarProgress() {
        if (AudioPlayer.isCreated()) {
            if (canUpdateTimeBar) {
                int progress = (int) (AudioPlayer.getCurrentPosition() /
                        (double) AudioPlayer.getDuration() * 1000);

                timeBar.setProgress(progress);
            }
        } else {
            timeBar.setProgress(0);
        }
    }


    //Перевод миллисекунд в формат hh:mm:ss
    private String millisecToTime(int millisec){
        return String.format(Locale.US,                      //Студия говорит, что надо ставить
                "%02d:%02d:%02d",                                   //Locale чтобы избежать багов. ОК
                millisec / (3600 * 1000),
                (millisec / (60 * 1000)) % 60,
                (millisec / (1000) % 60));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_CHOOSER_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    audioFilePath = uri.getPath();

                    updateTimeText();
                    updateTimeBarProgress();

                    Log.i(TAG, "File path: " + audioFilePath);
                    textViewPath.setText(audioFilePath);
                }
                break;
            }

            default:
                break;
        }
    }

}

