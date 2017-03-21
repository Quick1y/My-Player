package com.example.nikita.myplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class PlayerActivity extends Activity {
    private final static String TAG = "PlayerActivity";

    private final static String PATH_KEY = "path";

    private ImageButton playButton;
    private TextView textViewTrackName;
    private TextView textViewAlbumName;
    private SeekBar timeBar;
    private TextView textViewCurrTime;
    private TextView textViewDurTime;

    private String audioFilePath;
    private ProgressChangeTask progressTask; //AsyncTask, отслеживающий прогресс трека
    private boolean canUpdateTimeBar = true; // false при перетаскивании ползунка


    public static Intent newIntent(Context packageContext , String path){
        Intent intent = new Intent(packageContext, PlayerActivity.class);
        intent.putExtra(PATH_KEY, path);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        if(intent != null){
            audioFilePath = intent.getStringExtra(PATH_KEY);
        } else {
            return;
        }


        // Создание плеера:  плеер нужно создавать сразу  !!!
        try {
            AudioPlayer.create(audioFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
        }


        // Название трека:  Пока устанавливает в TextView путь к файлу
        textViewTrackName = (TextView) findViewById(R.id.activity_player_trackName_text);
        String name = audioFilePath;
        textViewTrackName.setText(name);

        // Название альбома
        textViewAlbumName = (TextView) findViewById(R.id.activity_player_albumName_text);
        textViewAlbumName.setText("Неизвестный альбом"); // Пока неизвестный


        // Текстовое поле время:  текущее и общее время трека
        textViewCurrTime = (TextView) findViewById(R.id.activity_player_currtime_text);
        textViewDurTime = (TextView) findViewById(R.id.activity_player_durtime_text);
        if(AudioPlayer.isCreated()){
            textViewDurTime.setText(millisecToTime(AudioPlayer.getDuration()));
        } else {
            textViewDurTime.setText(millisecToTime(0));
        }


        // Кнопка play / pause
        playButton = (ImageButton) findViewById(R.id.activity_player_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AudioPlayer.isPlaying()){
                    onPauseClicked();

                    //смена иконки на play
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow, getTheme()));
                    } else {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow));
                    }

                } else {
                    onPlayClicked();

                    //смена иконки на pause
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
                    } else {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    }
                }
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(AudioPlayer.isCreated()){
            progressTask.stop();
            AudioPlayer.destroy();
        }
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

}

