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
/*
    public static String parserDate(String s){
        HashMap<String, String>[] re=null;
        String temp="";
        String ret="";
        try {
            JSONObject jsonObj = new JSONObject(s);
            temp = jsonObj.getString("refreshment");
            JSONArray jsonObjs = new JSONArray(temp);
            int count =jsonObjs.length();
            if(count>0){
                JSONObject jb = jsonObjs.getJSONObject(0);
                ret=jb.getString("date");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }*/

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
