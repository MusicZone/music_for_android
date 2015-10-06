package com.weshi.imusic.imusicapp.main;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.weshi.imusic.imusicapp.R;


import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * Created by apple28 on 15/8/15.
 */
class MusicListAdapter extends SimpleCursorAdapter {

    private ImusicService service;
    public ImageButton playedView;
    private Context mContext;
    private CallBack mCallBack;
    private Cursor mCursor;



    public interface CallBack {
        public void refresh();
    }


    public MusicListAdapter(Context context,CallBack call, int layout, Cursor c,
                            String[] from, int[] to,ImusicService service) {
        super(context, layout, c, from, to);
        this.service = service;
        this.mContext = context;
        this.mCallBack = call;
        this.mCursor = c;
    }

    public void bindView(View view, Context context, Cursor cursor) {

        super.bindView(view, context, cursor);

        TextView titleView = (TextView) view.findViewById(R.id.text1);
        TextView artistView = (TextView) view.findViewById(R.id.text2);
        ImageButton playpausebutton = (ImageButton)view.findViewById(R.id.playpause);
        ImageButton deletebutton = (ImageButton)view.findViewById(R.id.delete);

        titleView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));

        artistView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));

        final String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        deletebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showInfo(path,id);
            }
        });

        playpausebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (playedView == v) {
                    if (service != null && service.isPlayingL()) {
                        service.pauseL();
                        v.setBackgroundResource(R.drawable.play);
                    } else if (service != null) {

                        service.startL();
                        v.setBackgroundResource(R.drawable.pause);
                    }
                } else {
                    service.setDataSourceL(path);
                    service.startL();

                    if (playedView != null)
                        playedView.setBackgroundResource(R.drawable.play);
                    v.setBackgroundResource(R.drawable.pause);

                    playedView = (ImageButton) v;
                }

            }
        });

        //int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
    }

    public void showInfo(final String mypath,final int id){
        new AlertDialog.Builder(mContext)
                .setTitle("确认删除文件？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        File file = new File(mypath);
                        if (file.isFile() && file.exists()) {
                            file.delete();
                            mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.Audio.Media._ID + "=" + id,
                                    null);


                            if(MusicListAdapter.this.mCallBack != null) {
                                MusicListAdapter.this.mCallBack.refresh();
                            }
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
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