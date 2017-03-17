package com.example.nikita.myplayer;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

/*
AsyncTask, реализующий обновление ProgressBar timeBar
и TextView textViewCurrTime в PlayerActivity.java
*/

class ProgressChangeTask extends AsyncTask<MediaPlayer, Integer,Void> {

    private static final String TAG = "PlayerTask";
    private ArrayList<OnProgressChangeListener> listenersList = new ArrayList<>();
    private boolean shouldStopped = false;

    public interface OnProgressChangeListener{
        void onProgressChange();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener){
        listenersList.add(listener);
    }

    //Вызывается когда нужно срочно, но корректно остановить Task
    public void stop(){
        shouldStopped = true;
    }

    @Override
    protected Void doInBackground(MediaPlayer... params) {
        Log.i(TAG, "doInBackground started");

        MediaPlayer player = (MediaPlayer) params[0];

        int duration = player.getDuration(); //Общая длина трека в миллисекундах
        int currentPosition; //Текущая позиция в миллисекундах

        do {
            currentPosition = player.getCurrentPosition();
            publishProgress(currentPosition * 1000 /duration, currentPosition); //обновляет ProgressBar и TextView

            try {
                /* Обновление данных 10 раз в секунду
                   Скорее всего так делать не правильно.      !!!
                   А как правильно нужно еще найти. */
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (!shouldStopped && currentPosition <= duration && player.isPlaying());

        Log.i(TAG, "doInBackground stopped");
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        //оповещает все слушателей
        for (OnProgressChangeListener listener : listenersList){
            listener.onProgressChange();
        }
    }
}