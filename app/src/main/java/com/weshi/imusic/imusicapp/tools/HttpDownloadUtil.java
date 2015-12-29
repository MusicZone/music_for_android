package com.weshi.imusic.imusicapp.tools;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;


/**
 * Created by apple28 on 15-8-3.
 */



public class HttpDownloadUtil extends AsyncTask<String,Integer,String>  {

    private HashMap<String, String>[] files;
    private CallBack mCallBack;
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private byte[] buffer;
    private int curr = 0;
    private int steps = 0;
    private int steplengh = 0;
    private long from=0;
    private long block = 0;
    private long fz = 0;
    private int times = 0;
    private Boolean s_finished = false;
    private HashMap<Integer,Boolean> allparts;
    private FileUtils fileUtils=new FileUtils();



    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
        public X509Certificate[] getAcceptedIssuers(){return null;}
        public void checkClientTrusted(X509Certificate[] certs, String authType){}
        public void checkServerTrusted(X509Certificate[] certs, String authType){}
    }};

    final String TAG="HttpDownloadUtil";



    public interface CallBack {
        void notifyResult(boolean re);
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();
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
        /*try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        for (HashMap<String, String> file : this.files)
        {

            if(isCancelled())
                return null;

            //publishProgress(current);
            for(int i=1;i<=10;i++) {
                if(isCancelled())
                    return null;

                String urlstr = "url"+String.valueOf(i);
                String sizestr = "size"+String.valueOf(i);
                String md5str = "md"+String.valueOf(i);
                String url = file.get(urlstr);
                String md5 = file.get(md5str);
                if(TextUtils.isEmpty(url) || url.equals("null")){
                    continue;
                }
                if(TextUtils.isEmpty(md5) || md5.equals("null")){
                    md5 = null;
                }

                Log.d("download", "s1:"+String.valueOf(i));
                int re = downFile(url, "imusic/", file.get("name"),md5, Long.valueOf(file.get(sizestr)).longValue(),current,step);
                Log.d("download", "s2:"+String.valueOf(re));
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

    public int downFile(String urlstr,String path,String fileName,String md5, long filesize,int current,int thisstep){
        InputStream inputStream=null;


        fileUtils.createSDDir(path);


        if (FileUtils.isFileExist(path,fileName)){
            int progressive =  current +  thisstep;
            publishProgress(progressive);
            return 1;
        }else{
            if(FileUtils.getSpace()<filesize) return -1;


            Log.d("download", "s3:");
            curr = current;
            steplengh = thisstep;
            times = 1000;
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            long start = 0;
            int trys = 10;


            HashMap<String,Long> map = new HashMap<String, Long>();
            while(trys>0) {

                if(isCancelled()) {
                    map =null;
                    allparts =null;
                    buffer = null;
                    return -1;
                }

                int re = getFileSize(urlstr, map);

                if (re == 0) {
                    fz = map.get("size");
                    block = fz;
                    break;
                } else if (re == 1) {
                    fz = map.get("size");
                    block = 524288;
                    break;
                } else
                    trys--;

            }
            map = null;

            Log.d("download", "s4:"+String.valueOf(trys));
            if(trys<=0)
                return 0;
//================
            //File resultFile = new File(path+fileName);
            //,to=0;
            final String url = urlstr;

            int count=1;
            if (fz/block!=0) {
                steps = (int)(fz/block)+1;
            }else{
                steps = (int)(fz/block);
            }
            //to = from + block-1;


            buffer = new byte[(int)fz];
            allparts = new HashMap<Integer, Boolean>();

            s_finished = false;
            from =0;


            class MyTask implements Runnable {
                String str;
                String url;
                long from;
                long block;
                int idx;
                MyTask(String url,long from,long block,int idx) { this.url = url;this.from = from; this.block = block; this.idx = idx; }
                public void run() {
                    Log.d("download", "s5:" +url+"-a-"+String.valueOf(from)+"-"+String.valueOf(block)+"-"+String.valueOf(idx));
                    downloadByThread(url, from, block, idx);
                    Log.d("download", "s6:");
                }
            }

            for(int thnum=0;thnum<steps;thnum++){
                final int idx = thnum;
                if(fz-1>=from+block-1){


                    Thread t = new Thread(new MyTask(url,from,block,idx));
                    t.start();

                    /*
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //handler.sendEmptyMessageDelayed(5, 3000);
                            //long end;
                            Log.d("mulithread", url+"-1-"+String.valueOf(from)+"-"+String.valueOf(block)+"-"+String.valueOf(idx));
                            //downloadByThread(url,from,block,idx);


                        }
                    }).start();*/
                    from += block;
                    //end = to+block-1;
                }else{
                    /*
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //handler.sendEmptyMessageDelayed(5, 3000);
                            //long end;
                            Log.d("mulithread", url+"-2-"+String.valueOf(from)+"-"+String.valueOf(fz-from)+"-"+String.valueOf(idx));
                            //downloadByThread(url,from,fz-from,idx);
                        }
                    }).start();*/
                    Thread t = new Thread(new MyTask(url,from,fz-from,idx));
                    t.start();
                    from = fz;
                    //end = fz-1;
                }
            }

            while(!s_finished){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };





/*

            while (from+block<=fz && trytime != 0) {
                inputStream=getInputStreamFormUrl(urlstr,from,to);

                resultFile=fileUtils.writeToSDfromInput(path, fileName, inputStream,block);
                if(resultFile==null){
                    trytime--;
                }else{
                    from +=block;
                    to +=block;
                    int progressive =  current +  (thisstep*count)/step;
                    publishProgress(progressive);
                    count++;
                }
                //long tt = resultFile.length();

                //tt=0;



            }
            while(from<fz && trytime != 0){
                inputStream=getInputStreamFormUrl(urlstr,from,fz-1);

                resultFile=fileUtils.writeToSDfromInput(path, fileName, inputStream,fz - from);
                if(resultFile==null){
                    trytime--;
                }else{

                    from =fz;
                    to =fz;
                    int progressive =  current +  thisstep;
                    publishProgress(progressive);
                }

            }*/

            if(buffer != null) {

                if(md5 != null){

                    Log.d("download", "s7:");
                    //int len = buffer.length;
                    String checksum = fileUtils.md5sum(buffer);
                    if(!checksum.equals(md5)){
                        Log.d("download", "s8:");
                        allparts =null;
                        buffer = null;
                        return 0;
                    }

                }
                fileUtils.writeToSDfromData(path, fileName,buffer,fz);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(new File(FileUtils.getFilePath(path, fileName))));
                mContext.sendBroadcast(scanIntent);
                buffer = null;
                allparts =null;
                return 1;
            }else{
                Log.d("download", "s9:");
                allparts =null;
                buffer = null;
                //resultFile.delete();
                return 0;
            }
        }

    }
    public void downloadByThread(String url,long start,long size,int index)
    {
        if(isCancelled()) {
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.obj = null;
            msg.sendToTarget();
            return;
        }

        InputStream inputStream=getInputStreamFormUrl(url,start,start+size-1);

        if(fileUtils.writeToArrayfromInput(inputStream,buffer,start,size)==0){
            //handler.sendEmptyMessage(index);
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.arg1 = (int)start;
            msg.arg2 = (int)size;
            msg.obj = url;
            msg.sendToTarget();
        }else{
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.arg1 = (int)start;
            msg.arg2 = (int)size;
            msg.obj = null;
            msg.sendToTarget();
/*
            from +=block;
            to +=block;
            int progressive =  current +  (thisstep*count)/step;
            publishProgress(progressive);
            count++;*/

        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {



            String url = (String)msg.obj;
            int from = msg.arg1;
            int block = msg.arg2;
            int myidx = msg.what;

            if(isCancelled()) {
                buffer = null;
                s_finished = true;
                return;
            }

            if(url != null){
                allparts.put(msg.what,true);

                int gets = allparts.size();

                int progressive =  curr +  (steplengh*gets)/steps;
                publishProgress(progressive);

                if(gets == steps){
                    s_finished = true;
                }
            }else{
                if(times>0) {
                    times--;
                    downloadByThread(url, from, block, myidx);
                }else{
                    buffer = null;
                    s_finished = true;
                }

            }


            //if (msg.what == 3 || msg.what == 5) {
                //tvMes.setText("what=" + msg.what + "，这是一个空消息");
            //} else {
               //tvMes.setText("what=" + msg.what + "," + msg.obj.toString());

            //}
        }
    };
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
/*

            MediaPlayer mMediaPlayerI =new MediaPlayer();

            mMediaPlayerI.setDataSource(urlstr);
            mMediaPlayerI.prepareAsync();// prepare();
*/

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

/*
            MediaPlayer mMediaPlayerI = new MediaPlayer();
            mMediaPlayerI.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayerI.setDataSource(urlstr);
            mMediaPlayerI.prepare();
*/

            //int tt = urlConn.getResponseCode();
            if(urlConn.getResponseCode()==200 || urlConn.getResponseCode()==206){
                long fs = urlConn.getContentLength();
                //Map<String, List<String>> tt = urlConn.getHeaderFields();
                if(urlConn.getHeaderField("Content-Type").equals("text/html"))
                    return -1;

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