package com.weshi.imusic.imusicapp.main;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
//import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import com.weshi.imusic.imusicapp.R;
import com.weshi.imusic.imusicapp.tools.FileUtils;
import com.weshi.imusic.imusicapp.tools.HttpDownloadUtil;

import android.os.Environment;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LocalActivity extends ListActivity {


    private ImusicService mMusicPlayerService = null;
    private MusicInfoController mMusicInfoController = null;
    private Cursor mCursor = null;
    private MusicListAdapter mAdapter;
    private ListView lv;
    //private TextView mTextView = null;
    //private Button mPlayPauseButton = null;
    //private Button mStopButton = null;


    private ServiceConnection mPlaybackConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mMusicPlayerService = ((ImusicService.LocalBinder)service).getService();

            mCursor = mMusicInfoController.getAllSongs();


            //ListAdapter adapter = new MusicListAdapter(this, android.R.layout.simple_expandable_list_item_2, mCursor, new String[]{}, new int[]{});
            mAdapter = new MusicListAdapter(
                    LocalActivity.this,
                    new MusicListAdapter.CallBack(){
                        public void refresh() {
                            mAdapter.notifyDataSetChanged();
                        }
                        public void scroll() {
                            int a = lv.getLastVisiblePosition();
                            a = mAdapter.nowpos;
                            if(lv.getLastVisiblePosition() == mAdapter.nowpos //last in the view
                                    && lv.getLastVisiblePosition() !=(lv.getCount()-1)){ //not down to bottom
                                //lv.setSelection(mAdapter.nowpos + 1);
                                lv.smoothScrollToPosition(mAdapter.nowpos + 1);
                            }
                        }
                    },
                    R.layout.list_item,
                    mCursor,
                    new String[]{},
                    new int[]{},
                    mMusicPlayerService
            );

            setListAdapter(mAdapter);
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
            if (action.equals(ImusicService.PLAYER_PREPARE_END_L)) {
                // will begin to play
                //mTextView.setVisibility(View.INVISIBLE);
                //mPlayPauseButton.setVisibility(View.VISIBLE);
                //mStopButton.setVisibility(View.VISIBLE);

                //mPlayPauseButton.setText(R.string.pause);
            } else if(action.equals(ImusicService.PLAY_COMPLETED_L)) {
                //mPlayPauseButton.setText(R.string.play);
                //if(mAdapter.playedView != null)
                //    mAdapter.playedView.setBackgroundResource(R.drawable.play);
                mAdapter.playNext();
                if(lv.getLastVisiblePosition() == mAdapter.nowpos //last in the view
                        && lv.getLastVisiblePosition() !=(lv.getCount()-1)){ //not down to bottom
                    lv.smoothScrollToPosition(mAdapter.nowpos + 1);
                }
            }/* else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
                //mPlayPauseButton.setText(R.string.play);
                screenoff = true;
            }*/

        }
    };
    public void stopMusic() {
        if (mMusicPlayerService != null && mMusicPlayerService.isPlayingL()) {
            mMusicPlayerService.pauseL();
            mAdapter.playedView.setBackgroundResource(R.drawable.play);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);
        mMusicInfoController = MusicInfoController.getInstance(this);

        startService(new Intent(this, ImusicService.class));
        getApplicationContext().bindService(new Intent(this, ImusicService.class), mPlaybackConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ImusicService.PLAYER_PREPARE_END_L);
        filter.addAction(ImusicService.PLAY_COMPLETED_L);

        //filter.addAction(Intent.ACTION_SCREEN_ON);
        //filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(mPlayerEvtReceiver, filter);
        lv = getListView();

/*
        ListView lv = getListView();
        lv.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() != (view.getCount() - 1)) {
                            view.setSelection(view.getSelectedItemPosition()+1);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });*/

    }
    protected void onResume() {
        super.onResume();

        //screenoff = false;
    }
    protected void onPause(){
        super.onPause();/*
        if (mMusicPlayerService != null && mMusicPlayerService.isPlayingL() && !screenoff) {
            mMusicPlayerService.pauseL();
            mAdapter.playedView.setBackgroundResource(R.drawable.play);
        }*/
    }
/*
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (mCursor == null ||mCursor.getCount() == 0) {
            return;
        }
        mCursor.moveToPosition(position);
        String url = mCursor
                .getString(mCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        mMusicPlayerService.setDataSourceL(url);
        mMusicPlayerService.startL();
    }*/

    @Override
    protected void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(mPlaybackConnection);
        stopMusic();
    }

}