package com.weshi.imusic.imusicapp.main;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;

public class MusicInfoController {
    private static MusicInfoController mInstance = null;

    private Activity pAct = null;

    public static MusicInfoController getInstance(Activity act) {
        if (mInstance == null) {
            mInstance = new MusicInfoController(act);
        }
        return mInstance;
    }


    private MusicInfoController(Activity act) {

        pAct = act;
    }

    public Activity getMusicPlayer() {
        return pAct;
    }

    private Cursor query(Uri uri, String[] prjs, String selections, String[] selectArgs, String order) {
        ContentResolver resolver = pAct.getContentResolver();
        if (resolver == null) {
            return null;
        }
        return resolver.query(uri, prjs, selections, selectArgs, order);
    }

    public Cursor getAllSongs() {
        return query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }
}

