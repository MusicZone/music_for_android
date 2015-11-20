package com.weshi.imusic.imusicapp.main;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.weshi.imusic.imusicapp.tools.NullHostNameVerifier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ImusicService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mMediaPlayerI = null;
    private MediaPlayer mMediaPlayerL = null;

    public static final String PLAYER_PREPARE_END_I = "com.weshi.imusic.iprepared";
    public static final String PLAY_COMPLETED_I = "com.weshi.imusic.iplaycompleted";
    public static final String PLAYER_PREPARE_END_L = "com.weshi.imusic.lprepared";
    public static final String PLAY_COMPLETED_L = "com.weshi.imusic.lplaycompleted";
    public static final String PLAY_ERROR_I = "com.weshi.imusic.ierror";
/*
    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
        public X509Certificate[] getAcceptedIssuers(){return null;}
        public void checkClientTrusted(X509Certificate[] certs, String authType){}
        public void checkServerTrusted(X509Certificate[] certs, String authType){}
    }};*/


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

    MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener()
    {
        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2)
        {
            // TODO Auto-generated method stub
            broadcastEvent(PLAY_ERROR_I);
            return false;
        }
    };


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
/*            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());*/
/*
            URL url=new URL(path);
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();
            //urlConn.setRequestMethod("HEAD");
            urlConn.connect();

*/

            //mMediaPlayerI.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayerI.setDataSource(path);
            mMediaPlayerI.prepare();
            //mMediaPlayerI.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e) {
            return;
        }/*catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/
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

