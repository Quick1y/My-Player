package com.example.nikita.myplayer.Model;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private static final String HANDLER_CURR_TIME = "AudioPlayer.HANDLER_CURR_TIME";
    private static final String HANDLER_DURATION = "AudioPlayer.HANDLER_DURATION";


    static private AudioPlayer audioPlayer;
    private MediaPlayer player;
    private Track mTrack;

    private static HashMap<Context, OnChangeListener> listeners = new HashMap<>();

    private boolean shouldStopped; //true, когда нужно срочно, но корректно остановить Task
    private boolean mPlayerStoped; //true, если плеер был остановлен /stop()/
    private Thread mProgressTread;
    private static final Handler mHandler;
    private boolean mPrepared; //false, когда плеер не готов (prepare())

    static {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                int currTime = bundle.getInt(HANDLER_CURR_TIME);
                int duration = bundle.getInt(HANDLER_DURATION);

                //оповещает все слушателей
                Set<Context> contexts = listeners.keySet();
                for (Context c : contexts) {
                    listeners.get(c).onProgressChange(currTime, duration);
                }
            }
        };
    }

    //приватный конструктуор, чтобы создать плеер можно было только через getInstance()
    private AudioPlayer() {/**/}

    public static AudioPlayer getInstance() {
        if (audioPlayer != null) {
            return audioPlayer;
        } else {
            audioPlayer = new AudioPlayer();
            return audioPlayer;
        }
    }

    //пересоздает плеер и проигрывает трек по указанному адресу
    //Методы ЖЦ MediaPlayer описаны на developer.android.com
    public void play(Track track) throws IOException {

        if (track == null) throw new IOException("source is null");
        if (mTrack!= null && track.getId() == mTrack.getId()) return;

        if(mProgressTread != null && mProgressTread.isAlive()){
            mProgressTread.interrupt(); //если старый тред есть, то сначала его убиваем
        }

        mPrepared = false;

        mTrack = track;

        if (player != null) {
            player.stop();
            player.release();
        }

        player = new MediaPlayer();

        player.reset();
        player.setDataSource(mTrack.getPath());

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                try {
                    play();
                    mPrepared = true;
                    //оповещаем всех слушателей, что плеер готов
                    Set<Context> contexts = listeners.keySet();
                    for (Context c : contexts) {
                        listeners.get(c).onPlayerPrepared();
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        player.prepareAsync();

        //оповешает всех слушаетелей о том, что трек закончился
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                shouldStopped = true;
                Set<Context> contexts = listeners.keySet();
                for (Context c : contexts) {
                    listeners.get(c).onPlayerCompletion();
                }
            }
        });

        Log.i(TAG, "Player created for file '" + mTrack.getPath() + "'");
    }

    public void destroy() {
        if (player != null) {
            shouldStopped = true;
            player.stop();
            player.release();
            player = null;
        }
    }


    //Устанавливает указанное время
    public void setCurrTime(int time) {
        if(time > getDuration() && getCurrTime() == getDuration() || time < 0){
            Log.i(TAG, "Seek cancel");
            return;
        }

        player.seekTo(time);
        Log.i(TAG, "Seek to " + time + "ms");
    }

    //Устанавливает прогресс в выбранную позицию
    public void setProgress(int progress) {
        int millisec = (int) (getDuration() * (progress / 1000.0)); //получает миллисекунды из progress = (0 - 1000)
        setCurrTime(millisec); //переход к указанной позиции
        Log.i(TAG, "Seek to " + progress / 10.0 + "%");
    }

    //нужно вызывать prepare() если был вызван stop()
    public void play() throws PlayerNotCreateException, IOException {
        if (player == null) throw new PlayerNotCreateException();
        if (player.isPlaying()) return;

        if (mPlayerStoped) {
            player.prepare();
            mPlayerStoped = false;
        }

        if(getCurrTime() == getDuration()){
            setCurrTime(0);
        }

        player.start();
        shouldStopped = false;

        //запускаем тред, обновляющий UI
        startProgressThread();
    }

    public void pause() throws PlayerNotCreateException {
        if (player != null) {
            player.pause();
        } else {
            throw new PlayerNotCreateException();
        }
    }

    public void stop() throws PlayerNotCreateException {
        if (player != null) {
            mPlayerStoped = true;
            player.stop();
        } else {
            throw new PlayerNotCreateException();
        }
    }


    public boolean isPlaying() {
        return player.isPlaying();
    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }

    public boolean isCreated() {
        return player != null;
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getCurrTime() {
        return player.getCurrentPosition();
    }

    public Track getTrack() {
        return mTrack;
    }

    public boolean isPrepared() {
        return mPrepared;
    }


    //отслежевание прогресса ниже

    public interface OnChangeListener {
        void onProgressChange(int currTime, int duration);

        void onPlayerPrepared();

        void onPlayerCompletion();
    }

    public void setOnChangeListener(OnChangeListener listener, Context context) {
        listeners.put(context, listener);
    }

    private void startProgressThread() {
        if(mProgressTread != null && mProgressTread.isAlive())
            return;

        mProgressTread = new Thread(new Runnable() {
            private int i; //только для вывода логов на каждый тридцатый i

            @Override
            public void run() {
                Log.i(TAG, "Progress thread " + hashCode() + " started");

                int duration = player.getDuration(); //Общая длина трека в миллисекундах
                int currentPosition; //Текущая позиция в миллисекундах

                do {
                    if(Thread.currentThread().isInterrupted())
                        break;

                    try {
                        currentPosition = player.getCurrentPosition();
                    } catch (IllegalStateException e) {
                        currentPosition = 0;
                        e.printStackTrace();
                    }

                    //обновляет ProgressBar и TextView
                    Bundle bundle = new Bundle();
                    Message message = mHandler.obtainMessage();
                    bundle.putInt(HANDLER_CURR_TIME, currentPosition);
                    bundle.putInt(HANDLER_DURATION, duration);
                    message.setData(bundle);
                    mHandler.sendMessage(message);

                    i++;
                    if (i % 50 == 0){
                        Log.d(TAG, "Progress thread " + hashCode() + " alive, listeners count: " + listeners.size());
                    }


                    if(!Thread.currentThread().isInterrupted()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100); // тормозим поток на 100 миллисекунд
                        } catch (InterruptedException e) {
                            //Исключение может произойти, если interrupt() будет вызван во время sleep()
                            e.printStackTrace();
                            break;
                        }
                    }

                } while (!shouldStopped && currentPosition <= duration);

                Log.i(TAG, "Progress thread " + hashCode() + " stopped");
            }
        });

        mProgressTread.start();
    }

    public void unSubscribeChangeListener(Context context) {
        listeners.remove(context);
    }
}



