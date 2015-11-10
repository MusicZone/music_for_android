package com.weshi.imusic.imusicapp.tools;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;





import java.io.FileOutputStream;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by apple28 on 15-8-3.
 */
public class HttpDownloadUtil extends AsyncTask<String,Integer,String>  {

    private HashMap<String, String>[] files;
    private CallBack mCallBack;
    private ProgressDialog mProgressDialog;
    private Context mContext;

    final String TAG="HttpDownloadUtil";



    public interface CallBack {
        void notifyResult(boolean re);
    }

    public HttpDownloadUtil(Context mx,HashMap<String, String>[] fls,CallBack callback,ProgressDialog bar)
    {
        this.files = fls;
        this.mCallBack = callback;
        this.mProgressDialog = bar;
        this.mContext = mx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //showDialog(DIALOG_DOWNLOAD_PROGRESS);
    }


    @Override
    protected String doInBackground(String... aurl) {

        int current = 0;
        int step=100/this.files.length;
        for (HashMap<String, String> file : this.files)
        {

            publishProgress(current);
            for(int i=1;i<=10;i++) {
                String urlstr = "url"+String.valueOf(i);
                String sizestr = "size"+String.valueOf(i);
                String url = file.get(urlstr);
                if(TextUtils.isEmpty(url) || url.equals("null")){
                    break;
                }
                int re = downFile(url, "imusic/", file.get("name"), Long.valueOf(file.get(sizestr)).longValue());
                if(re == 1){
                    break;
                }
            }
            current+=step;
            Log.d(TAG, file.get("name"));
        }

        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        //Log.d(LOG_TAG,progress[0]);
        //mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String unused) {
        super.onPostExecute(unused);

        mProgressDialog.dismiss();
        if(mCallBack != null)
            this.mCallBack.notifyResult(true);
        //dismiss the dialog after the file was downloaded
        //dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
    }

    /**
     * 该函数返回整形    -1：代表下载文件错误0 ：下载文件成功1：文件已经存在
     * @param urlstr
     * @param path
     * @param fileName
     * @return
     */
    public int downFile(String urlstr,String path,String fileName,long filesize){
        InputStream inputStream=null;
        FileUtils fileUtils=new FileUtils();

        fileUtils.createSDDir(path);


        if (FileUtils.isFileExist(path,fileName)){
            return 1;
        }else{
            if(FileUtils.getSpace()<filesize) return -1;
            inputStream=getInputStreamFormUrl(urlstr);
            File resultFile=fileUtils.writeToSDfromInput(path, fileName, inputStream);
            if(resultFile==null){
                return 0;
            }
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(new File(FileUtils.getFilePath(path, fileName))));
            mContext.sendBroadcast(scanIntent);
            return 1;
        }

    }

    /**
     * 根据URL得到输入流
     * @param urlstr
     * @return
     */
    public InputStream getInputStreamFormUrl(String urlstr){
        InputStream inputStream=null;
        try {
            URL url=new URL(urlstr);
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();
            inputStream=urlConn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
}