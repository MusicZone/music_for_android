package com.weshi.imusic.imusicapp.main;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import com.weshi.imusic.imusicapp.tools.FileUtils;
import com.weshi.imusic.imusicapp.tools.HttpDownloadUtil;
import com.weshi.imusic.imusicapp.update.UpdateManager;
import com.weshi.imusic.imusicapp.tools.JsonUtil;

import com.weshi.imusic.imusicapp.R;

public class ImusicActivity extends Activity implements HttpDownloadUtil.CallBack {


    private ImusicService mMusicPlayerService = null;
    private MusicInfoController mMusicInfoController = null;
    private Cursor mCursor = null;

    private TextView mTextView = null;
    private Button mPlayPauseButton = null;
    private Button mStopButton = null;
    private ProgressDialog progressDialog = null;

    private HashMap<String, String>[] playHeads;
    private HashMap<String, String>[] playAlbums;

    private int SongID = 0;
    private boolean headPlay=false;



    private static final int QUERY_ABSTRACT_SUCCESS = 1;
    private static final int QUERY_ABSTRACT_FAILURE = 2;
    private static final int QUERY_ALBUMS_SUCCESS = 3;
    private static final int QUERY_ALBUMS_FAILURE = 4;

    private JsonUtil jsonparser=new JsonUtil();
    final String TAG="ImusicActivity";





    private ServiceConnection mPlaybackConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mMusicPlayerService = ((ImusicService.LocalBinder)service).getService();
            Thread mAbstractThread=new Thread(AbstractInfo);
            mAbstractThread.start();
        }
        public void onServiceDisconnected(ComponentName className)
        {
            mMusicPlayerService = null;
        }
    };

    protected BroadcastReceiver mPlayerEvtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ImusicService.PLAYER_PREPARE_END)) {
                // will begin to play
                /*
                mTextView.setVisibility(View.INVISIBLE);
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setText(R.string.pause);*/


            } else if(action.equals(ImusicService.PLAY_COMPLETED)) {
                //mPlayPauseButton.setText(R.string.play);
                if(headPlay){
                    headPlay = false;

                    if(SongID<playAlbums.length) {
                        HashMap<String, String> det = playAlbums[SongID];

                        if (FileUtils.isFileExist("imusic/", det.get("name"))) {
                            String aurl = FileUtils.getFilePath("imusic/", det.get("name"));
                            Log.d(TAG, "After Head, play song:" + aurl + "songid:" + SongID);
                            mMusicPlayerService.setDataSource(aurl);
                            mMusicPlayerService.start();
                        }else
                            Log.d(TAG, "File do not exist!");
                    }
                }else{
                    SongID++;
                    if(playHeads.length>SongID){
                        HashMap<String, String> det = playHeads[SongID];
                        String aurl = det.get("url");

                        headPlay=true;
                        Log.d(TAG, "After Song, play head:"+aurl);
                        mMusicPlayerService.setDataSource(aurl);
                        mMusicPlayerService.start();

                    }else{
/*
                        headPlay = false;

                        if(SongID>=playAlbums.length) SongID=0;

                        HashMap<String, String> det = playAlbums[SongID];
                        String aurl = FileUtils.getFilePath("imusic/", det.get("name"));
                        Log.d(TAG, "No head, play song:"+aurl);
                        mMusicPlayerService.setDataSource(aurl);
                        mMusicPlayerService.start();*/
                    }

                }
            }
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imusic);

        mMusicInfoController = MusicInfoController.getInstance(this);

        startService(new Intent(this, ImusicService.class));
        boolean re = getApplicationContext().bindService(new Intent(this, ImusicService.class), mPlaybackConnection, Context.BIND_AUTO_CREATE);

/*
        mTextView = (TextView)findViewById(R.id.show_text);
        mPlayPauseButton = (Button) findViewById(R.id.play_pause_btn);
        mStopButton = (Button) findViewById(R.id.stop_btn);

        mPlayPauseButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (mMusicPlayerService != null && mMusicPlayerService.isPlaying()) {
                    mMusicPlayerService.pause();
                    mPlayPauseButton.setText(R.string.play);
                } else if (mMusicPlayerService != null){
                    mMusicPlayerService.start();
                    mPlayPauseButton.setText(R.string.pause);
                }
            }
        });

        mStopButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (mMusicPlayerService != null ) {
                    mTextView.setVisibility(View.VISIBLE);
                    mPlayPauseButton.setVisibility(View.INVISIBLE);
                    mStopButton.setVisibility(View.INVISIBLE);
                    mMusicPlayerService.stop();
                }
            }
        });*/

        IntentFilter filter = new IntentFilter();
        filter.addAction(ImusicService.PLAYER_PREPARE_END);
        filter.addAction(ImusicService.PLAY_COMPLETED);
        registerReceiver(mPlayerEvtReceiver, filter);
    }

    protected void onResume() {
        super.onResume();
        /*
        mCursor = mMusicInfoController.getAllSongs();

        ListAdapter adapter = new MusicListAdapter(this, android.R.layout.simple_expandable_list_item_2, mCursor, new String[]{}, new int[]{});
        setListAdapter(adapter);*/
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(mPlaybackConnection);
    }

    public void notifyResult(boolean re)
    {
        if(re){

            if(mMusicPlayerService.isPlaying()) return;
            HashMap<String, String> det = playAlbums[SongID];

            String aurl = FileUtils.getFilePath("imusic/", det.get("name"));

            Log.d(TAG, "First time, No head now, play song:"+aurl);
            mMusicPlayerService.setDataSource(aurl);
            mMusicPlayerService.start();

        }
    }






    Runnable AbstractInfo=new Runnable(){
        @Override
        public void run() {
            try {
                String url=getText(R.string.Server_Url)+"m=Abstracts&a=get";
                HttpGet httpRequest = new HttpGet(url);
                String strResult = "";
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpRequest);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    strResult = EntityUtils.toString(httpResponse.getEntity());
                }
                playHeads = jsonparser.parserAbstract(strResult);
                mHandler.obtainMessage(QUERY_ABSTRACT_SUCCESS).sendToTarget();
            } catch (Exception e) {
                mHandler.obtainMessage(QUERY_ABSTRACT_FAILURE).sendToTarget();
                return;
            }
        }
    };

    Runnable AlbumsInfo=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                String url=getText(R.string.Server_Url)+"m=Albums&a=get";
                HttpGet httpRequest = new HttpGet(url);
                String strResult = "";
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpRequest);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    strResult = EntityUtils.toString(httpResponse.getEntity());
                }

                playAlbums = jsonparser.parserAlbums(strResult);
                mHandler.obtainMessage(QUERY_ALBUMS_SUCCESS).sendToTarget();
            } catch (Exception e) {
                mHandler.obtainMessage(QUERY_ALBUMS_FAILURE).sendToTarget();
                return;
            }
        }
    };



    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QUERY_ABSTRACT_SUCCESS:


                    Thread mAlbumsThread=new Thread(AlbumsInfo);
                    mAlbumsThread.start();

                    break;
                case QUERY_ALBUMS_SUCCESS:

                    if(playHeads.length>0){
                        HashMap<String, String> det = playHeads[SongID];
                        String aurl = det.get("url");

                        headPlay=true;


                        Log.d(TAG, "Get list and play first head:"+aurl);
                        mMusicPlayerService.setDataSource(aurl);
                        mMusicPlayerService.start();
                    }


                    progressDialog = new ProgressDialog(ImusicActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("正在同步歌曲...");
                    progressDialog.setProgress(0);
                    progressDialog.setMax(100);
                    progressDialog.show();






                    HttpDownloadUtil downloadMusic = new HttpDownloadUtil(playAlbums,ImusicActivity.this,progressDialog);
                    downloadMusic.execute();
                    break;
                default:
                    break;
            }
        }


    };


}

