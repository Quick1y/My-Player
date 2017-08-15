package com.example.nikita.myplayer.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.Model.AudioPlayer;
import com.example.nikita.myplayer.Model.PlayerNotCreateException;
import com.example.nikita.myplayer.Utils.ProgressChangeTask;
import com.example.nikita.myplayer.R;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PlayerActivity extends Activity {
    private final static String TAG = "PlayerActivity";

    private final static String INTENT_KEY_PATH_KEY = "PlayerActivity.INTENT_KEY_PATH_KEY";
    private static final String SAVE_PROGRESS = "PlayerActivity.SAVE_PROGRESS";

    private ImageButton playButton;
    private ImageButton mSeekForwardButton;
    private ImageView mSeekBackButton;

    private TextView textViewTrackName;
    private TextView textViewAlbumName;
    private SeekBar timeBar;
    private TextView textViewCurrTime;
    private TextView textViewDurTime;
    private ImageView mAlbumImageView;

    private String audioFilePath;
    private ProgressChangeTask progressTask; //AsyncTask, отслеживающий прогресс трека

    private boolean canUpdateTimeBar = true; // false при перетаскивании ползунка
    private boolean hasAlbum; // true если имеет обложку альбома

    private int widthScrollX; // шарина по коодинате Х для прокрутки изображения альбома


    public static Intent newIntent(Context packageContext, String path) {
        Intent intent = new Intent(packageContext, PlayerActivity.class);
        intent.putExtra(INTENT_KEY_PATH_KEY, path);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Установка темы
        setTheme(R.style.PlayerActivityTheme_orange);

        Intent intent = getIntent();

        if (intent != null) {
            audioFilePath = intent.getStringExtra(INTENT_KEY_PATH_KEY);
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
        String name;
        if(audioFilePath != null){
            name = new File(audioFilePath).getName();
        } else {
            name = "Нет трека";
        }

        textViewTrackName.setText(name);

        // Название альбома
        textViewAlbumName = (TextView) findViewById(R.id.activity_player_albumName_text);
        textViewAlbumName.setText(audioFilePath); // Пока неизвестный


        // Текстовое поле время:  текущее и общее время трека
        textViewCurrTime = (TextView) findViewById(R.id.activity_player_currtime_text);
        textViewDurTime = (TextView) findViewById(R.id.activity_player_durtime_text);
        if (AudioPlayer.isCreated()) {
            textViewDurTime.setText(millisecToTime(AudioPlayer.getDuration()));
        } else {
            textViewDurTime.setText(millisecToTime(0));
        }


        // Кнопка play / pause
        playButton = (ImageButton) findViewById(R.id.activity_player_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AudioPlayer.isPlaying()) {
                    onPauseClicked();
                } else {
                    onPlayClicked();
                }
            }
        });

        //SeekBar: инициализируется и устанавливается в 0
        timeBar = (SeekBar) findViewById(R.id.activity_player_seekBar);
        timeBar.setMax(1000);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int progMs = (int) (AudioPlayer.getDuration() * (progress / 1000.0));
                    textViewCurrTime.setText(millisecToTime(progMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { canUpdateTimeBar = false; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (AudioPlayer.isCreated()) {
                    canUpdateTimeBar = true;
                    int progress = seekBar.getProgress();
                    AudioPlayer.setProgress(progress < 5 ? 0 : progress);
                }
            }
        });


        mAlbumImageView = (ImageView) findViewById(R.id.activity_player_album_image);
        mAlbumImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlbumImageView.getDrawable() != null) {
                    mAlbumImageView.setImageDrawable(null);
                    hasAlbum = false;
                } else {
                    mAlbumImageView.setImageResource(R.drawable.album_ex);
                    int x = mAlbumImageView.getDrawable().getIntrinsicWidth(); //mW
                    x = x - mAlbumImageView.getWidth(); // mW - ivW = dX
                    x = x - x/2;

                    widthScrollX = x;

                    hasAlbum = true;
                }

            }
        });




        mSeekForwardButton = (ImageButton) findViewById(R.id.activity_player_seek_forward_button);
        mSeekForwardButton.setOnClickListener(new View.OnClickListener() {
            Toast toast;
            long lastShown;
            int sumSeekInterval;
            //показывает Toast так, что пока он висит, время обновляется в нем
            @Override
            public void onClick(View view) {
                int seekInterval = 10000;
                AudioPlayer.setCurrTime(AudioPlayer.getCurrTime() + seekInterval);
                String mess;

                if(System.currentTimeMillis() < lastShown + 2000 ){
                    sumSeekInterval += 10000;
                    mess = String.format(getString(R.string.pa_seek_forward), String.valueOf((seekInterval+sumSeekInterval)/1000));
                    toast.setText(mess);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                } else {
                    mess = String.format(getString(R.string.pa_seek_forward), String.valueOf(seekInterval/1000));
                    toast = Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                    sumSeekInterval = 0;
                }


            }
        });

        mSeekBackButton = (ImageButton) findViewById(R.id.activity_player_seek_back_button);
        mSeekBackButton.setOnClickListener(new View.OnClickListener() {
            Toast toast;
            long lastShown;
            int sumSeekInterval;
            //показывает Toast так, что пока он висит, время обновляется в нем
            @Override
            public void onClick(View view) {
                int seekInterval = 10000;
                AudioPlayer.setCurrTime(AudioPlayer.getCurrTime() - seekInterval);
                String mess;

                if(System.currentTimeMillis() < lastShown + 2000){
                    sumSeekInterval += 10000;
                    mess = String.format(getString(R.string.pa_seek_back), String.valueOf((seekInterval + sumSeekInterval)/1000));
                    toast.setText(mess);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                } else {
                    mess = String.format(getString(R.string.pa_seek_back), String.valueOf(seekInterval/1000));
                    toast = Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                    sumSeekInterval = 0;
                }
            }
        });

        if(savedInstanceState != null){
            int time = savedInstanceState.getInt(SAVE_PROGRESS);
            AudioPlayer.setCurrTime(time);
        }

        updateTimeText();
        updateTimeBarProgress();
        onPlayClicked();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AudioPlayer.isCreated()) {
            progressTask.stop();
            AudioPlayer.destroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        int time = AudioPlayer.getCurrTime();
        bundle.putInt(SAVE_PROGRESS, time);
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


        //Анимация ниже:
        //смена иконки на pause
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playButton.setImageDrawable(getResources()
                    .getDrawable(R.drawable.anim_play_pause_new, getTheme()));
            Drawable drawable = playButton.getDrawable();

            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        }
    }

    //вызывается при клике на Кнопку Стоп
    private void onPauseClicked() {
        try {
            AudioPlayer.pause();
        } catch (PlayerNotCreateException e) {
            e.printStackTrace();
            return;
        }

        //Анимация ниже:
        //смена иконки на play
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playButton.setImageDrawable(getResources()
                    .getDrawable(R.drawable.anim_pause_play_new, getTheme()));
            Drawable drawable = playButton.getDrawable();

            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
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
            if (canUpdateTimeBar) {
                String time = millisecToTime(AudioPlayer.getCurrTime());
                textViewCurrTime.setText(time);
                //Log.i(TAG, "Time: " + time);
            }
        } else {
            textViewCurrTime.setText(millisecToTime(0));
        }

    }

    //Устанавливает прогресс в SeekBar timeBar
    public void updateTimeBarProgress() {
        if (AudioPlayer.isCreated()) {
            if (canUpdateTimeBar) {
                int progress = (int) (AudioPlayer.getCurrTime() /
                        (double) AudioPlayer.getDuration() * 1000);

                timeBar.setProgress(progress);

                if(progress == 1000){
                    AudioPlayer.setProgress(0);
                    onPauseClicked();
                    updateTimeText();
                    updateTimeBarProgress();
                }

                Configuration conf = getResources().getConfiguration();
                if(hasAlbum && conf.orientation == conf.ORIENTATION_PORTRAIT){
                    int scrollTo = (widthScrollX * progress)/1000 - widthScrollX/2;
                    mAlbumImageView.scrollTo(scrollTo,0);
                }

            }
        } else {
            //timeBar.setProgress(0);
        }

    }


    //Перевод миллисекунд в формат hh:mm:ss
    private String millisecToTime(int millisec) {
        return String.format(Locale.US,                      //Студия говорит, что надо ставить
                "%02d:%02d:%02d",                                   //Locale чтобы избежать багов. ОК
                millisec / (3600 * 1000),
                (millisec / (60 * 1000)) % 60,
                (millisec / (1000) % 60));
    }
}

