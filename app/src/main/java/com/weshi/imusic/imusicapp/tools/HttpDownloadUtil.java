package com.weshi.imusic.imusicapp.tools;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
                int re = downFile(url, "imusic/", file.get("name"), Long.valueOf(file.get(sizestr)).longValue(),current,step);
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

    public int downFile(String urlstr,String path,String fileName,long filesize,int current,int thisstep){
        InputStream inputStream=null;
        FileUtils fileUtils=new FileUtils();

        fileUtils.createSDDir(path);


        if (FileUtils.isFileExist(path,fileName)){
            return 1;
        }else{
            if(FileUtils.getSpace()<filesize) return -1;




            long start = 0;
            long block = 0;
            HashMap<String,Long> map = new HashMap<String, Long>();
            int re = getFileSize(urlstr,map);
            long fz = 0;
            if(re == 0){
                fz = map.get("filesize");
                block = fz;
            }else if(re == 1){
                fz = map.get("size");
                block = 524288;
            }else
                return 0;


//================
            File resultFile = new File(path+fileName);
            long from=0,to=0;
            int trytime = 100;
            int step=0,count=1;
            if (fz/block!=0) {
                step = (int)(fz/block)+1;
            }else{
                step = (int)(fz/block);
            }
            to = from + block-1;
            while (from+block<=fz && trytime != 0) {
                inputStream=getInputStreamFormUrl(urlstr,from,to);

                resultFile=fileUtils.writeToSDfromInput(path, fileName, inputStream);
                if(resultFile==null){
                    trytime--;
                }else{
                    from +=block;
                    to +=block;
                    int progressive =  current +  (thisstep*count)/step;
                    publishProgress(progressive);
                    count++;
                }
                long tt = resultFile.length();

                tt=0;



            }
            while(from<fz && trytime != 0){
                inputStream=getInputStreamFormUrl(urlstr,from,fz-1);

                resultFile=fileUtils.writeToSDfromInput(path, fileName, inputStream);
                if(resultFile==null){
                    trytime--;
                }else{

                    from +=block;
                    to +=block;
                    int progressive =  current +  thisstep;
                    publishProgress(progressive);
                }

            }

            if(resultFile.length()==fz && trytime !=0 ) {

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(new File(FileUtils.getFilePath(path, fileName))));
                mContext.sendBroadcast(scanIntent);
                return 1;
            }else{
                resultFile.delete();
                return 0;
            }
        }

    }

    /**
     * 根据URL得到输入流
     * @param urlstr
     * @return
     */
    public InputStream getInputStreamFormUrl(String urlstr,long start,long end){
        InputStream inputStream=null;
        try {
            URL url=new URL(urlstr);
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("Range","bytes="+start+"-"+end);
            inputStream=urlConn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
    public int getFileSize(String urlstr, HashMap<String,Long> result){

        try {
            URL url=new URL(urlstr);
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("HEAD");
            urlConn.connect();
            if(urlConn.getResponseCode()==200 || urlConn.getResponseCode()==206){
                long fs = urlConn.getContentLength();
                if(fs <=0) return -1;
                else if(!urlConn.getHeaderField("Accept-Ranges").equals("bytes")){
                    result.put("size",fs);
                    return 0;
                }else {
                    result.put("size",fs);
                    return 1;
                }
            }else{
                return -1;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }
}