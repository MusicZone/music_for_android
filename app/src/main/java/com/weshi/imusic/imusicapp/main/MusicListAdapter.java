package com.weshi.imusic.imusicapp.main;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.weshi.imusic.imusicapp.R;

/**
 * Created by apple28 on 15/8/15.
 */
class MusicListAdapter extends SimpleCursorAdapter {


    public MusicListAdapter(Context context, int layout, Cursor c,
                            String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    public void bindView(View view, Context context, Cursor cursor) {

        super.bindView(view, context, cursor);

        TextView titleView = (TextView) view.findViewById(R.id.text1);
        TextView artistView = (TextView) view.findViewById(R.id.text2);

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