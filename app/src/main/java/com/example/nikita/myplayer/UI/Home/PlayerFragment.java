package com.example.nikita.myplayer.UI.Home;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.Database.TrackDataBase;
import com.example.nikita.myplayer.Model.AudioPlayer;
import com.example.nikita.myplayer.Model.PlayerNotCreateException;
import com.example.nikita.myplayer.Model.Track;
import com.example.nikita.myplayer.R;

import java.io.IOException;
import java.util.Locale;

public class PlayerFragment extends Fragment {
    private final static String TAG = "PlayerFragment";

    private static final String ARG_TRACK_ID = "PlayerFragment.ARG_TRACK_ID";


    private ImageButton playButton;
    private ImageButton mSeekForwardButton;
    private ImageView mSeekBackButton;

    private TextView textViewTrackName;
    private TextView textViewArtistName;
    private SeekBar timeBar;
    private TextView textViewCurrTime;
    private TextView textViewDurTime;
    private ImageView mAlbumImageView;

    private int mTrackId;
    private Track mTrack;
    private AudioPlayer mAudioPlayer;

    private boolean canUpdateTimeBar = true; // false при перетаскивании ползунка
    private boolean hasAlbum; // true, если имеет обложку альбома

    private int widthScrollX; // ширина по коодинате Х для прокрутки изображения альбома



    public static PlayerFragment newInstance(int trackId) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrackId = getArguments().getInt(ARG_TRACK_ID);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        mTrack = TrackDataBase.getTrackById(getActivity(), mTrackId);


        //создаем плеер
        mAudioPlayer = AudioPlayer.getInstance();
        if(mAudioPlayer.isCreated() && mTrack == null){
            mTrack = mAudioPlayer.getTrack();
        }


        // Название трека:  Пока устанавливает в TextView путь к файлу
        textViewTrackName = (TextView) view.findViewById(R.id.activity_player_trackName_text);
        String name;
        if (mTrack != null) {
            name = mTrack.getName();
        } else {
            name = "Нет трека";
        }

        textViewTrackName.setText(name);

        // Название альбома
        textViewArtistName = (TextView) view.findViewById(R.id.activity_player_artistName_text);
        textViewArtistName.setText(
                (mTrack != null && mTrack.getArtist() != null)?
                        mTrack.getArtist() : "Неизвестный исполнитель"); // Пока неизвестный


        // Текстовое поле время:  текущее и общее время трека
        textViewCurrTime = (TextView) view.findViewById(R.id.activity_player_currtime_text);
        textViewDurTime = (TextView) view.findViewById(R.id.activity_player_durtime_text);

        // Кнопка play / pause
        playButton = (ImageButton) view.findViewById(R.id.activity_player_play_button);
        playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_new));
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    onPauseClicked();
                } else {
                    onPlayClicked();
                }
            }
        });

        //SeekBar: инициализируется и устанавливается в 0
        timeBar = (SeekBar) view.findViewById(R.id.activity_player_seekBar);
        timeBar.setMax(1000);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mAudioPlayer.isPrepared()) {
                    int progMs = (int) (mAudioPlayer.getDuration() * (progress / 1000.0));
                    textViewCurrTime.setText(millisecToHour(progMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mAudioPlayer.isCreated() && mAudioPlayer.isPrepared()) {
                    canUpdateTimeBar = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mAudioPlayer.isCreated() && mAudioPlayer.isPrepared()) {
                    canUpdateTimeBar = true;
                    int progress = seekBar.getProgress();
                    mAudioPlayer.setProgress(progress <= 5 ? 0 : progress);
                }
            }
        });


        mAlbumImageView = (ImageView) view.findViewById(R.id.activity_player_album_image);
        mAlbumImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlbumImageView.getDrawable() != null) {
                    mAlbumImageView.setImageDrawable(null);
                    hasAlbum = false;
                } else {
                    mAlbumImageView.setImageResource(R.drawable.album_ex);

                    int drawableWx = mAlbumImageView.getHeight();
                    int screenWx = mAlbumImageView.getWidth(); // ширина экрана
                    widthScrollX  =  drawableWx - screenWx;

                    Log.d(TAG, "drawable width = " + drawableWx);
                    Log.d(TAG, "screen width = " + screenWx);
                    Log.d(TAG, "wsX = " + widthScrollX);


                    hasAlbum = true;

                    Configuration conf = getResources().getConfiguration();
                    if (hasAlbum && conf.orientation == conf.ORIENTATION_PORTRAIT) {
                        int scrollTo = - widthScrollX/2;
                        mAlbumImageView.scrollTo(scrollTo, 0);
                    }
                }

            }
        });


        mSeekForwardButton = (ImageButton) view.findViewById(R.id.activity_player_seek_forward_button);
        mSeekForwardButton.setOnClickListener(new View.OnClickListener() {
            Toast toast;
            long lastShown;
            int sumSeekInterval;

            //показывает Toast так, что пока он висит, время обновляется в нем
            @Override
            public void onClick(View view) {
                int seekInterval = 10000;
                mAudioPlayer.setCurrTime(mAudioPlayer.getCurrTime() + seekInterval);

                if (System.currentTimeMillis() < lastShown + 2000) {
                    sumSeekInterval += 10000;
                    toast.setText("+ " + millisecToMinute(seekInterval + sumSeekInterval));
                    toast.show();
                    lastShown = System.currentTimeMillis();
                } else {
                    toast = Toast.makeText(getActivity(), "+ " + millisecToMinute(seekInterval), Toast.LENGTH_SHORT);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                    sumSeekInterval = 0;
                }
                updateTimeBarProgress(mAudioPlayer.getCurrTime(), mAudioPlayer.getDuration());
                updateTimeText(mAudioPlayer.getCurrTime());
            }
        });

        mSeekBackButton = (ImageButton) view.findViewById(R.id.activity_player_seek_back_button);
        mSeekBackButton.setOnClickListener(new View.OnClickListener() {
            Toast toast;
            long lastShown;
            int sumSeekInterval;

            //показывает Toast так, что пока он висит, время обновляется в нем
            @Override
            public void onClick(View view) {
                int seekInterval = 10000;
                mAudioPlayer.setCurrTime(mAudioPlayer.getCurrTime() - seekInterval);

                if (System.currentTimeMillis() < lastShown + 2000) {
                    sumSeekInterval += 10000;
                    toast.setText("- " + millisecToMinute(seekInterval+sumSeekInterval));
                    toast.show();
                    lastShown = System.currentTimeMillis();
                } else {
                    toast = Toast.makeText(getActivity(), "- " + millisecToMinute(seekInterval), Toast.LENGTH_SHORT);
                    toast.show();
                    lastShown = System.currentTimeMillis();
                    sumSeekInterval = 0;
                }
                updateTimeBarProgress(mAudioPlayer.getCurrTime(), mAudioPlayer.getDuration());
                updateTimeText(mAudioPlayer.getCurrTime());
            }
        });


        //если уже играет и запускается без пути, или играет именно выбранный трек
        if(mAudioPlayer.isCreated() &&
                (mTrack == null || mTrack.getId() == mAudioPlayer.getTrack().getId())){
            String duration = millisecToHour(mAudioPlayer.getDuration());
            textViewDurTime.setText(duration);
            // в данном случае просто устанавливает иконку в кнопку Play
            if (mAudioPlayer.isPlaying()){
                changeIconOnPause();
            } else {
                changeIconOnPlay();
            }

        } else if(mTrack != null){ //если не играет
            try {
                mAudioPlayer.play(mTrack);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Файл не найден", Toast.LENGTH_SHORT).show();
            }
        }

        //события, происходящие с плеером
        mAudioPlayer.setOnChangeListener(new AudioPlayer.OnChangeListener() {
            @Override
            public void onProgressChange(int currTime, int duration) {
                updateTimeBarProgress(currTime, duration); // обновить прогресс для SeekBar timeBar
                updateTimeText(currTime);        // обновить текущее время для TextView textViewCurrTime
            }

            @Override
            public void onPlayerPrepared() {
                String duration = millisecToHour(mAudioPlayer.getDuration());
                textViewDurTime.setText(duration);
                changeIconOnPause();
            }

            @Override
            public void onPlayerCompletion() {
                onPauseClicked();
                changeIconOnPlay();
                Log.d(TAG, "onPlayerCompletion");
            }
        }, getActivity());




        return view;
    }

    @Override
    public void onDestroy() {
        mAudioPlayer.unSubscribeChangeListener(getActivity());
        super.onDestroy();
    }



    //вызывается при клике на Кнопку Плей
    private void onPlayClicked() {

        /* Если плеер не создан, то создает и запускает его.
        Если создан, то просто продолжает воспроизведение.
         */
        try {
            mAudioPlayer.play();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(getActivity(), "Невозможно воспроизвести файл", Toast.LENGTH_LONG).show();
            return;
        } catch (PlayerNotCreateException pncEx) {
            pncEx.printStackTrace();
            return;
        }


        //обновляет общую длину трека
        textViewDurTime.setText(millisecToHour(mAudioPlayer.getDuration()));

        //Меняем иконку
        changeIconOnPause();
    }

    //вызывается при клике на Кнопку Стоп
    private void onPauseClicked() {
        try {
            mAudioPlayer.pause();
        } catch (PlayerNotCreateException e) {
            e.printStackTrace();
            return;
        }

        //Меняем иконку
        changeIconOnPlay();
    }


    private void changeIconOnPlay(){
        //смена иконки на play
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playButton.setImageDrawable(getResources()
                    .getDrawable(R.drawable.anim_pause_play_new, getActivity().getTheme()));
            Drawable drawable = playButton.getDrawable();

            if (drawable instanceof Animatable)
                ((Animatable) drawable).start();

        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_new));
        }
    }

    private void changeIconOnPause(){
        //смена иконки на pause
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playButton.setImageDrawable(getResources()
                    .getDrawable(R.drawable.anim_play_pause_new, getActivity().getTheme()));
            Drawable drawable = playButton.getDrawable();

            if (drawable instanceof Animatable)
                ((Animatable) drawable).start();

        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_new));
        }
    }


    //Устанавливает текущее время в TextView textViewCurrTime
    public void updateTimeText(int currTime) {
        if (mAudioPlayer.isCreated()) {
            if (canUpdateTimeBar) {
                String time = millisecToHour(currTime);
                textViewCurrTime.setText(time);
                //Log.i(TAG, "Time: " + time);
            }
        } else {
            textViewCurrTime.setText(millisecToHour(0));
        }

    }

    //Устанавливает прогресс в SeekBar timeBar
    public void updateTimeBarProgress(int currTime, int duration) {
        if (mAudioPlayer.isCreated()) {
            if (canUpdateTimeBar) {
                int progress = (int) (currTime / (double) duration * 1000);

                timeBar.setProgress(progress);

                //скроллит картинку альбома
                Configuration conf = getResources().getConfiguration();
                if (hasAlbum && conf.orientation == conf.ORIENTATION_PORTRAIT) {
                    int scrollTo = (widthScrollX * progress) / 1000 - widthScrollX/2;
                    mAlbumImageView.scrollTo(scrollTo, 0);
                }
            }
        }
    }


    //Перевод миллисекунд в формат hh:mm:ss
    private String millisecToHour(int millisec) {
        return String.format(Locale.US,                      //Студия говорит, что надо ставить
                "%02d:%02d:%02d",                                   //Locale чтобы избежать багов. ОК
                millisec / (3600 * 1000),
                (millisec / (60 * 1000)) % 60,
                (millisec / (1000) % 60));
    }

    //Перевод миллисекунд в формат mm:ss
    private String millisecToMinute(int millisec) {
        return String.format(Locale.US,                      //Студия говорит, что надо ставить
                "%02d:%02d",                                   //Locale чтобы избежать багов. ОК
                (millisec / (60 * 1000)),
                (millisec / (1000) % 60));
    }
}

