package com.weshi.imusic.imusicapp.main;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class ImusicService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mMediaPlayer = null;

    public static final String PLAYER_PREPARE_END = "com.weshi.imusic.prepared";
    public static final String PLAY_COMPLETED = "com.weshi.imusic.playcompleted";


    MediaPlayer.OnCompletionListener mCompleteListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            broadcastEvent(PLAY_COMPLETED);
        }
    };

    MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            broadcastEvent(PLAYER_PREPARE_END);
        }
    };


    MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener()
    {
        @Override
          /*覆盖错误处理事件*/
        public boolean onError(MediaPlayer arg0, int arg1, int arg2)
        {
            // TODO Auto-generated method stub

            return false;
        }
    };


    private void broadcastEvent(String what) {
        Intent i = new Intent(what);
        sendBroadcast(i);
    }


    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mPrepareListener);
        mMediaPlayer.setOnCompletionListener(mCompleteListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
    }

    public class LocalBinder extends Binder {
        public ImusicService getService() {
            return ImusicService.this;
        }
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void setDataSource(String path) {

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            return;
        } catch (IllegalArgumentException e) {
            return;
        }
    }


    public void start() {
        mMediaPlayer.start();
    }


    public void stop() {
        mMediaPlayer.stop();
    }


    public void pause() {
        mMediaPlayer.pause();
    }


    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }


    public int getDuration() {
        return mMediaPlayer.getDuration();
    }


    public int getPosition() {
        return mMediaPlayer.getCurrentPosition();
    }


    public long seek(long whereto) {
        mMediaPlayer.seekTo((int) whereto);
        return whereto;
    }
}

