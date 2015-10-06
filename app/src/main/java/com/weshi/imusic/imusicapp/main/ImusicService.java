package com.weshi.imusic.imusicapp.main;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class ImusicService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mMediaPlayerI = null;
    private MediaPlayer mMediaPlayerL = null;

    public static final String PLAYER_PREPARE_END_I = "com.weshi.imusic.iprepared";
    public static final String PLAY_COMPLETED_I = "com.weshi.imusic.iplaycompleted";
    public static final String PLAYER_PREPARE_END_L = "com.weshi.imusic.lprepared";
    public static final String PLAY_COMPLETED_L = "com.weshi.imusic.lplaycompleted";

    MediaPlayer.OnCompletionListener mCompleteListenerI = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            broadcastEvent(PLAY_COMPLETED_I);
        }
    };

    MediaPlayer.OnPreparedListener mPrepareListenerI = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            broadcastEvent(PLAYER_PREPARE_END_I);
        }
    };
    MediaPlayer.OnCompletionListener mCompleteListenerL = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            broadcastEvent(PLAY_COMPLETED_L);
        }
    };

    MediaPlayer.OnPreparedListener mPrepareListenerL = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            broadcastEvent(PLAYER_PREPARE_END_L);
        }
    };
/*
    MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener()
    {
        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2)
        {
            // TODO Auto-generated method stub

            return false;
        }
    };
*/

    private void broadcastEvent(String what) {
        Intent i = new Intent(what);
        sendBroadcast(i);
    }


    public void onCreate() {
        super.onCreate();

        mMediaPlayerI = new MediaPlayer();
        mMediaPlayerI.setOnPreparedListener(mPrepareListenerI);
        mMediaPlayerI.setOnCompletionListener(mCompleteListenerI);
        //mMediaPlayerI.setOnErrorListener(mErrorListenerI);
        mMediaPlayerL = new MediaPlayer();
        mMediaPlayerL.setOnPreparedListener(mPrepareListenerL);
        mMediaPlayerL.setOnCompletionListener(mCompleteListenerL);
        //mMediaPlayerL.setOnErrorListener(mErrorListenerL);
    }

    public class LocalBinder extends Binder {
        public ImusicService getService() {
            return ImusicService.this;
        }
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setDataSourceI(String path) {

        try {
            mMediaPlayerI.reset();
            mMediaPlayerI.setDataSource(path);
            mMediaPlayerI.prepare();
        } catch (IOException e) {
            return;
        } catch (IllegalArgumentException e) {
            return;
        }
    }
    public void setDataSourceL(String path) {

        try {
            mMediaPlayerL.reset();
            mMediaPlayerL.setDataSource(path);
            mMediaPlayerL.prepare();
        } catch (IOException e) {
            return;
        } catch (IllegalArgumentException e) {
            return;
        }
    }


    public void startI() {
        mMediaPlayerI.start();
    }


    public void stopI() {
        mMediaPlayerI.stop();
    }


    public void pauseI() {
        mMediaPlayerI.pause();
    }


    public boolean isPlayingI() {
        return mMediaPlayerI.isPlaying();
    }


    public int getDurationI() {
        return mMediaPlayerI.getDuration();
    }


    public int getPositionI() {
        return mMediaPlayerI.getCurrentPosition();
    }


    public long seekI(long whereto) {
        mMediaPlayerI.seekTo((int) whereto);
        return whereto;
    }
    public void startL() {
        mMediaPlayerL.start();
    }


    public void stopL() {
        mMediaPlayerL.stop();
    }


    public void pauseL() {
        mMediaPlayerL.pause();
    }


    public boolean isPlayingL() {
        return mMediaPlayerL.isPlaying();
    }


    public int getDurationL() {
        return mMediaPlayerL.getDuration();
    }


    public int getPositionL() {
        return mMediaPlayerL.getCurrentPosition();
    }


    public long seekL(long whereto) {
        mMediaPlayerL.seekTo((int) whereto);
        return whereto;
    }
}

