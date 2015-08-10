package com.weshi.imusic.imusicapp.tools;

/**
 * Created by apple28 on 15/8/10.
 */
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonUtil {

    public static HashMap<String, String>[] parserAbstract(String s){
        HashMap<String, String>[] re=null;
        try {
            JSONArray jsonObjs = new JSONArray(s);
            int count =jsonObjs.length();
            re = new HashMap[count];
            for(int i=0;i<count;i++){
                JSONObject jb = jsonObjs.getJSONObject(i);
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("name", jb.getString("name"));
                song.put("url", jb.getString("url"));
                re[i]=song;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return re;
    }
    public static HashMap<String, String>[] parserAlbums(String s){
        HashMap<String, String>[] re=null;
        try {
            JSONArray jsonObjs = new JSONArray(s);
            int count =jsonObjs.length();
            re = new HashMap[count];
            for(int i=0;i<count;i++){
                JSONObject jb = jsonObjs.getJSONObject(i);
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("name", jb.getString("name"));
                song.put("url", jb.getString("url"));
                song.put("size", jb.getString("size"));
                re[i]=song;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return re;
    }


}
