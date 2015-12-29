package com.weshi.imusic.imusicapp.main;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

import java.io.File;
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
import android.content.SharedPreferences;
import android.widget.Toast;

import com.weshi.imusic.imusicapp.R;

public class ImusicActivity extends Activity implements HttpDownloadUtil.CallBack {


    private ImusicService mMusicPlayerService = null;
    private MusicInfoController mMusicInfoController = null;
    private Cursor mCursor = null;

    private ImageButton mPlayPauseButton = null;
    private ProgressDialog progressDialog = null;
    private ProgressBar mProgressBar = null;
    private TextView mTextView = null;
    private HashMap<String, String>[] playHeads;
    private HashMap<String, String>[] playAlbums;

    private int SongID = 0;
    private boolean headPlay=false;
    private HttpDownloadUtil downloadMusic;



    private static final int QUERY_ABSTRACT_SUCCESS = 1;
    private static final int QUERY_ABSTRACT_FAILURE = 2;
    private static final int QUERY_ALBUMS_SUCCESS = 3;
    private static final int QUERY_ALBUMS_FAILURE = 4;

    private JsonUtil jsonparser=new JsonUtil();
    final String TAG="ImusicActivity";
    private String dataStr="";




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
            if (action.equals(ImusicService.PLAYER_PREPARE_END_I)) {
                // will begin to play
                /*
                mTextView.setVisibility(View.INVISIBLE);
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setText(R.string.pause);*/


            } else if(action.equals(ImusicService.PLAY_COMPLETED_I)) {
                mTextView.setText("");
                //mPlayPauseButton.setText(R.string.play);
                if(headPlay){


                    if(SongID<playAlbums.length) {
                        HashMap<String, String> det = playAlbums[SongID];
                        String aurl = FileUtils.getFilePath("imusic/", det.get("name"));
                        Log.d(TAG, "After Head, play song:" + aurl + "songid:" + SongID);
                        mMusicPlayerService.setDataSourceI(aurl);
                        if (FileUtils.isFileExist("imusic/", det.get("name"))) {
                            headPlay = false;

                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            try {
                                mmr.setDataSource(aurl);
                                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                if(title == null){
                                    title = det.get("name");
                                }
                                mTextView.setText(title);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mMusicPlayerService.startI();
                        }else{
                            headPlay = true;
                            Log.d(TAG, "File do not exist!");
/*
                            mPlayPauseButton.setOnClickListener(new Button.OnClickListener() {
                                public void onClick(View v) {
                                    // Perform action on click
                                    progressDialog = new ProgressDialog(ImusicActivity.this);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setTitle("正在同步歌曲...");
                                    progressDialog.setProgress(0);
                                    progressDialog.setMax(100);
                                    progressDialog.show();

                                    HttpDownloadUtil downloadMusic = new HttpDownloadUtil(ImusicActivity.this, playAlbums, ImusicActivity.this, progressDialog);
                                    downloadMusic.execute();
                                }
                            });*/
                            mPlayPauseButton.setBackgroundResource(R.drawable.play);
                            mPlayPauseButton.setVisibility(View.VISIBLE);
                            setTabClickable(true);






                            progressDialog = new ProgressDialog(getParent());
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("正在同步歌曲...");
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.show();

                            HttpDownloadUtil downloadMusic = new HttpDownloadUtil(ImusicActivity.this, playAlbums, ImusicActivity.this, progressDialog);
                            downloadMusic.execute();






                            //Toast.makeText(ImusicActivity.this,"请连接网络后重试!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        headPlay = false;
                        mPlayPauseButton.setBackgroundResource(R.drawable.play);
                        //SharedPreferences settings = getSharedPreferences("imusic", Activity.MODE_PRIVATE);
                        //SharedPreferences.Editor editor = settings.edit();
                        //editor.putString("mark", dataStr);
                        //editor.commit();

                        Thread mAbstractThread=new Thread(AbstractInfo);
                        mAbstractThread.start();
                        SongID=0;
                    }
                }else{
                    SongID++;
                    headPlay=true;
                    if(playHeads != null && playHeads.length>SongID){
                        HashMap<String, String> det = playHeads[SongID];
                        String aurl = det.get("url");


                        Log.d(TAG, "After Song, play head:"+aurl);
                        mMusicPlayerService.setDataSourceI(aurl);
                        mMusicPlayerService.startI();

                    }else{
                        mPlayPauseButton.setBackgroundResource(R.drawable.play);
                        //SharedPreferences settings = getSharedPreferences("imusic", Activity.MODE_PRIVATE);
                        //SharedPreferences.Editor editor = settings.edit();
                        //editor.putString("mark", dataStr);
                        //editor.commit();

                        Thread mAbstractThread=new Thread(AbstractInfo);
                        mAbstractThread.start();
                        SongID=0;
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
            } else if(action.equals(ImusicService.PLAY_ERROR_I)) {

            }
        }
    };
    private void setTabClickable(boolean en){
        TabWidget tb = ((TabActivity)getParent()).getTabHost().getTabWidget();
        tb.getChildAt(1).setClickable(en);
        tb.getChildAt(2).setClickable(en);//.getTabWidget().setEnabled(en); //.getChildAt(1).setClickable(en);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imusic);
        FileUtils.getMediaFiles(this);
        mMusicInfoController = MusicInfoController.getInstance(this);

        startService(new Intent(this, ImusicService.class));
        getApplicationContext().bindService(new Intent(this, ImusicService.class), mPlaybackConnection, Context.BIND_AUTO_CREATE);


        IntentFilter filter = new IntentFilter();
        filter.addAction(ImusicService.PLAYER_PREPARE_END_I);
        filter.addAction(ImusicService.PLAY_COMPLETED_I);
        filter.addAction(ImusicService.PLAY_ERROR_I);
        registerReceiver(mPlayerEvtReceiver, filter);



        mProgressBar = (ProgressBar) findViewById(R.id.waiting);
        mPlayPauseButton = (ImageButton) findViewById(R.id.imusicplay);
        mTextView = (TextView) findViewById(R.id.title);

    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(mPlaybackConnection);
        stopMusic();
    }

    @Override
    protected void onPause(){
        super.onPause();/*
        if(mMusicPlayerService !=null && mMusicPlayerService.isPlayingI())
        mMusicPlayerService.pauseI();
        mPlayPauseButton.setBackgroundResource(R.drawable.play);*/
    }
    public void stopMusic() {
        if(mMusicPlayerService !=null && mMusicPlayerService.isPlayingI()) {
            mMusicPlayerService.pauseI();
            mPlayPauseButton.setBackgroundResource(R.drawable.play);
        }
    }
    public void notifyResult(boolean re)
    {
        if(re){
            downloadMusic = null;
            mTextView.setText("");
            //Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //scanIntent.setData(Uri.fromFile(new File(FileUtils.getFilePath("imusic/", ""))));
            //sendBroadcast(scanIntent);
            mPlayPauseButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    if (mMusicPlayerService != null && mMusicPlayerService.isPlayingI()) {
                        mMusicPlayerService.pauseI();
                        mPlayPauseButton.setBackgroundResource(R.drawable.play);
                    } else if (mMusicPlayerService != null) {
                        mMusicPlayerService.startI();
                        mPlayPauseButton.setBackgroundResource(R.drawable.pause);
                    }
                }
            });
            mMusicPlayerService.startI();
            mPlayPauseButton.setBackgroundResource(R.drawable.pause);
            mPlayPauseButton.setVisibility(View.VISIBLE);
            //if(mMusicPlayerService.isPlaying()) return;
            //HashMap<String, String> det = playAlbums[SongID];


            //String aurl = FileUtils.getFilePath("imusic/", det.get("name"));
            //Log.d(TAG, "First time, No head now, play song:" + aurl);
            //mMusicPlayerService.setDataSource(aurl);
            //mMusicPlayerService.start();

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
                //dataStr = jsonparser.parserDate(strResult);
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


                    //SharedPreferences settings = getSharedPreferences("imusic", Activity.MODE_PRIVATE);
                    //String mark_recorded = settings.getString("mark", "");

                    //if(!mark_recorded.equals(dataStr)) {
                        Thread mAlbumsThread = new Thread(AlbumsInfo);
                        mAlbumsThread.start();
                    //}
                    break;
                case QUERY_ALBUMS_SUCCESS:


                    if(playHeads.length>0){
                        HashMap<String, String> det = playHeads[SongID];
                        String aurl = det.get("url");

                        headPlay=true;


                        Log.d(TAG, "Get list and play first head:" + aurl);
                        mMusicPlayerService.setDataSourceI(aurl);

                    }
                    mProgressBar.setVisibility(View.GONE);
                    mProgressBar.setEnabled(false);


                    mPlayPauseButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            // Perform action on click
                            progressDialog = new ProgressDialog(ImusicActivity.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle("正在同步歌曲...");
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if(downloadMusic != null){
                                        downloadMusic.cancel(true);
                                    }
                                }
                            });
                            progressDialog.show();

                            downloadMusic = new HttpDownloadUtil(ImusicActivity.this, playAlbums, ImusicActivity.this, progressDialog);
                            downloadMusic.execute();
                        }
                    });
                    mPlayPauseButton.setBackgroundResource(R.drawable.play);
                    mPlayPauseButton.setVisibility(View.VISIBLE);
                    setTabClickable(true);
                    mTextView.setText("播放过程需要使用网络流量，最好使用WIFI网络！");


                    break;
                case QUERY_ABSTRACT_FAILURE:
                case QUERY_ALBUMS_FAILURE:
                    mProgressBar.setVisibility(View.GONE);
                    mProgressBar.setEnabled(false);
                    setTabClickable(true);
                    mPlayPauseButton.setVisibility(View.VISIBLE);
                    mPlayPauseButton.setBackgroundResource(R.drawable.reload);// .seBackgroundResource(R.drawable.reload);
                    mPlayPauseButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            // Perform action on click
                            Thread mAbstractThread = new Thread(AbstractInfo);
                            mAbstractThread.start();
                            mPlayPauseButton.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mProgressBar.setEnabled(true);
                        }
                    });
                    Toast.makeText(ImusicActivity.this,"请连接网络后重新刷新歌单!",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }


    };


}

