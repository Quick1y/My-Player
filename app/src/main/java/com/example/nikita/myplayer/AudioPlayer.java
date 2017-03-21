package com.example.nikita.myplayer;


import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    static private MediaPlayer player;

    //приватный конструктуор, чтобы создать плеер можно было только через create()
    private AudioPlayer() {
    }

    public static void create(String source) throws IOException {
        //Методы ЖЦ MediaPlayer описаны на developer.android.com
        player = new MediaPlayer();
        player.reset();
        player.setDataSource(source);
        player.prepare();

        Log.i(TAG, "Player created!");
    }

    public static void destroy(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    //Устанавливает прогресс в выбранную позицию
    public static void setProgress(int progress){
        int millisec = (int) (getDuration() * (progress / 1000.0)); //получает миллисекунды из progress = (0 - 1000)
        player.seekTo(millisec); //переход к указанной позиции

        Log.i(TAG, "Seek to " + millisec + "ms, " + progress / 10.0 + "%");
    }

    //нужно вызывать prepare() если был вызван stop()
    public static void play() throws PlayerNotCreateException {
        if (player != null) {
            player.start();
        } else {
            throw new PlayerNotCreateException();
        }
    }

    public static void pause() throws PlayerNotCreateException {
        if (player != null) {
            player.pause();
        } else {
            throw new PlayerNotCreateException();
        }
    }

    public static void stop() throws PlayerNotCreateException {
        if (player != null) {
            player.stop();
        } else {
            throw new PlayerNotCreateException();
        }
    }


    public static boolean isPlaying(){
        return player.isPlaying();
    }

    public static MediaPlayer getPlayer() {
        return player;
    }

    public static boolean isCreated() {
        return player != null;
    }

    public static int getDuration() {
        return player.getDuration();
    }

    public static int getCurrentPosition() {
        return player.getCurrentPosition();
    }
}
