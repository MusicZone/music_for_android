package com.weshi.imusic.imusicapp.main;

import android.app.ListActivity;
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
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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

public class ImusicActivity extends ListActivity implements HttpDownloadUtil.CallBack {


    private ImusicService mMusicPlayerService = null;
    private MusicInfoController mMusicInfoController = null;
    private Cursor mCursor = null;

    private TextView mTextView = null;
    private Button mPlayPauseButton = null;
    private Button mStopButton = null;

    private HashMap<String, String>[] playHeads;
    private HashMap<String, String>[] playAlbums;

    private int SongID = 0;
    private boolean headPlay=false;



    private static final int QUERY_ABSTRACT_SUCCESS = 1;
    private static final int QUERY_ABSTRACT_FAILURE = 2;
    private static final int QUERY_ALBUMS_SUCCESS = 3;
    private static final int QUERY_ALBUMS_FAILURE = 4;

    private JsonUtil jsonparser=new JsonUtil();





    private ServiceConnection mPlaybackConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mMusicPlayerService = ((ImusicService.LocalBinder)service).getService();
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
                mTextView.setVisibility(View.INVISIBLE);
                mPlayPauseButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setText(R.string.pause);
            } else if(action.equals(ImusicService.PLAY_COMPLETED)) {
                mPlayPauseButton.setText(R.string.play);
                if(headPlay){
                    headPlay = false;

                    HashMap<String, String> det = playAlbums[SongID];
                    String aurl = FileUtils.getFilePath("imusic/",det.get("name"));
                    mMusicPlayerService.setDataSource(aurl);
                    mMusicPlayerService.start();
                }else{
                    if(playHeads.length>SongID){
                        HashMap<String, String> det = playHeads[SongID++];
                        String aurl = det.get("url");

                        headPlay=true;
                        mMusicPlayerService.setDataSource(aurl);
                        mMusicPlayerService.start();

                    }else{
                        SongID++;

                        headPlay = false;

                        if(playAlbums.length>=SongID) SongID=0;

                        HashMap<String, String> det = playAlbums[SongID];
                        String aurl = FileUtils.getFilePath("imusic/", det.get("name"));
                        mMusicPlayerService.setDataSource(aurl);
                        mMusicPlayerService.start();
                    }

                }
            }
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imusic);


        UpdateManager manager = new UpdateManager(this);
        // 检查软件更新
        manager.checkUpdate();


        Thread mAbstractThread=new Thread(AbstractInfo);
        mAbstractThread.start();






        mMusicInfoController = MusicInfoController.getInstance(this);

        startService(new Intent(this,ImusicService.class));
        bindService(new Intent(this,ImusicService.class), mPlaybackConnection, Context.BIND_AUTO_CREATE);


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
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ImusicService.PLAYER_PREPARE_END);
        filter.addAction(ImusicService.PLAY_COMPLETED);
        registerReceiver(mPlayerEvtReceiver, filter);
    }

    protected void onResume() {
        super.onResume();
        mCursor = mMusicInfoController.getAllSongs();

        ListAdapter adapter = new MusicListAdapter(this, android.R.layout.simple_expandable_list_item_2, mCursor, new String[]{}, new int[]{});
        setListAdapter(adapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (mCursor == null ||mCursor.getCount() == 0) {
            return;
        }
        mCursor.moveToPosition(position);
        String url = mCursor
                .getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        mMusicPlayerService.setDataSource(url);
        mMusicPlayerService.start();
    }







    public void notifyResult(boolean re)
    {
        if(re){

            HashMap<String, String> det = playAlbums[SongID];
            String url = det.get("url");

            mMusicPlayerService.setDataSource(url);
            mMusicPlayerService.start();

        }
    }






    Runnable AbstractInfo=new Runnable(){
        @Override
        public void run() {
            try {
                String url=getText(R.string.Server_Url)+"m=Abstract&a=get";
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

                    if(playHeads.length>0){
                        HashMap<String, String> det = playHeads[SongID];
                        String url = det.get("url");

                        headPlay=true;


                        mMusicPlayerService.setDataSource(url);
                        mMusicPlayerService.start();
                    }
                    Thread mAlbumsThread=new Thread(AlbumsInfo);
                    mAlbumsThread.start();

                    break;
                case QUERY_ALBUMS_SUCCESS:

                    HttpDownloadUtil downloadMusic = new HttpDownloadUtil(playAlbums,ImusicActivity.this);
                    downloadMusic.execute();
                    break;
                default:
                    break;
            }
        }


    };


}






/**********************************
 *
 *********************************/
class MusicListAdapter extends SimpleCursorAdapter {


    public MusicListAdapter(Context context, int layout, Cursor c,
                            String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    public void bindView(View view, Context context, Cursor cursor) {

        super.bindView(view, context, cursor);

        TextView titleView = (TextView) view.findViewById(android.R.id.text1);
        TextView artistView = (TextView) view.findViewById(android.R.id.text2);

        titleView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));

        artistView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));

        //int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
    }

    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }
}

