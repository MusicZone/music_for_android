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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;





import java.io.FileOutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    private ExecutorService pool;

    private Iterator md5It;
    private Iterator<String[]> urlIt;
    private String md5;
    HashMap<String, ArrayList<String[]>> mdmap;



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

        createHandler();

        /*try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        for (HashMap<String, String> file : this.files)
        {

            if(isCancelled())
                return null;

            int sum = Integer.valueOf(file.get("urls_count"));
            //publishProgress(current);

            mdmap = new HashMap<String, ArrayList<String[]>>();
            for(int i=1;i<=sum;i++) {
                if(isCancelled()){
                    return null;
                }
                String urlstr = "url"+String.valueOf(i);
                String sizestr = "size"+String.valueOf(i);
                String md5str = "md"+String.valueOf(i);
                String url = file.get(urlstr);
                String md5 = file.get(md5str);
                String size = file.get(sizestr);
                if(TextUtils.isEmpty(url) || url.equals("")){
                    continue;
                }
                if(TextUtils.isEmpty(md5) || md5.equals("") || md5.equals("0")){
                    continue;
                }
                if(TextUtils.isEmpty(size) || size.equals("") || size.equals("0")){
                    continue;
                }


                ArrayList<String[]> urls;
                if(mdmap.containsKey(md5)){
                    urls = mdmap.get(md5);
                }else{
                    urls = new ArrayList<String[]>();
                    mdmap.put(md5,urls);
                }
                String[] urlinfo = new String[2];
                urlinfo[0] = size;
                urlinfo[1] = url;
                urls.add(urlinfo);
            }
            addFourTimes(mdmap);
            //Log.d("download", "s1:"+String.valueOf(i));
            int re = downFile(mdmap, "imusic/", file.get("name"), current,step);
            //Log.d("download", "s2:"+String.valueOf(re));

            /*for(int i=1;i<=sum;i++) {
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
            }*/
            current+=step;
            Log.d(TAG, file.get("name"));
        }

        return null;
    }
    private void addFourTimes(HashMap<String, ArrayList<String[]>> map){
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            ArrayList<String[]> ar = (ArrayList<String[]>)entry.getValue();
            ar.addAll(ar);
            ar.addAll(ar);
        }

    }
    private void createHandler(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                handler = new Handler() {
                    @Override
                    public void handleMessage(android.os.Message msg) {



                        String[] urlwithmd = (String[])msg.obj;
                        String url = urlwithmd[0];
                        String md = urlwithmd[1];
                        int from = msg.arg1;
                        int block = msg.arg2;
                        int myidx = msg.what;

                        if(md != md5)
                            return;

                        if(isCancelled()) {
                            buffer = null;
                            s_finished = true;
                            return;
                        }

                        if(url == "" && allparts != null){
                            allparts.put(myidx,true);

                            int gets = allparts.size();
                            Log.d("mydownload", "threadcomplete:" + String.valueOf(gets)+"/"+String.valueOf(steps));

                            int progressive =  curr +  (steplengh*gets)/steps;
                            publishProgress(progressive);

                            if(gets == steps){
                                s_finished = true;
                            }
                        }else{
                            if(times>0) {
                                times--;


                                if(urlIt.hasNext()){
                                    String[] urlinfo = urlIt.next();
                                    String urlstr =urlinfo[1];
                                    long filesize = Long.valueOf(urlinfo[0]);


                                    if(filesize != 0 && FileUtils.getSpace()>filesize) {
                                        pool.execute(new MyTask(urlstr, from, block, myidx,md));
                                    }

                                }else{
                                    int checktime = 5;
                                    while(checktime>0) {
                                        while (md5It.hasNext()) {
                                            Map.Entry<String, ArrayList<String[]>> entry = (Map.Entry<String, ArrayList<String[]>>) md5It.next();
                                            String md5_tmp = entry.getKey();
                                            ArrayList<String[]> info = entry.getValue();
                                            urlIt = info.iterator();

                                            while (urlIt.hasNext()) {
                                                String[] urlinfo = urlIt.next();
                                                String urlstr = urlinfo[1];
                                                long filesize = Long.valueOf(urlinfo[0]);


                                                if(md5 == md5_tmp){
                                                    if(filesize != 0 && FileUtils.getSpace()>filesize) {
                                                        pool.execute(new MyTask(urlstr, from, block, myidx,md));
                                                        return;
                                                    }
                                                }else {
                                                    md5 = md5_tmp;


                                                    if (filesize == 0 || FileUtils.getSpace() < filesize)
                                                        continue;

                                                    String[] urlstrs = new String[5];
                                                    urlstrs[0] = urlstr;
                                                    if (urlIt.hasNext()) {
                                                        urlstrs[1] = urlIt.next()[1];
                                                    } else {
                                                        urlstrs[1] = urlstr;
                                                    }

                                                    if (urlIt.hasNext()) {
                                                        urlstrs[2] = urlIt.next()[1];
                                                    } else {
                                                        urlstrs[2] = urlstr;
                                                    }

                                                    if (urlIt.hasNext()) {
                                                        urlstrs[3] = urlIt.next()[1];
                                                    } else {
                                                        urlstrs[3] = urlstr;

                                                }
                                                    if (urlIt.hasNext()) {
                                                        urlstrs[4] = urlIt.next()[1];
                                                    } else {
                                                        urlstrs[4] = urlstr;
                                                    }

                                                    if (downloadByUrl(urlstrs, md) == 1) {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                        checktime--;
                                        md5It = mdmap.entrySet().iterator();
                                    }
                                }
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
                Looper.loop();
            }
        }).start();
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



    class MyTask implements Runnable {
        String str;
        String url;
        long from;
        long block;
        int idx;
        String md5;
        MyTask(String url,long from,long block,int idx,String md5) { this.url = url;this.from = from; this.block = block; this.idx = idx;this.md5 = md5; }
        public void run() {
            Log.d("download", "s5:" +url+"-a-"+String.valueOf(from)+"-"+String.valueOf(block)+"-"+String.valueOf(idx));
            downloadByThread(url, from, block, idx, md5);
            Log.d("download", "s6:");
        }
    }
    //public int downFile(String urlstr,String path,String fileName,String md5, long filesize,int current,int thisstep){
    public int downFile(HashMap<String, ArrayList<String[]>> md,String path,String fileName,int current,int thisstep){
        long filesize;
        String urlstr;
        InputStream inputStream=null;
        //Log.i("download", "sx:");
        fileUtils.createSDDir(path);
        if (FileUtils.isFileExist(path, fileName)){
            int progressive =  current +  thisstep;
            publishProgress(progressive);
            return 1;
        }else{
            curr = current;
            steplengh = thisstep;
            md5It = md.entrySet().iterator();
            while(md5It.hasNext()){
                Map.Entry<String, ArrayList<String[]>> entry = (Map.Entry<String, ArrayList<String[]>>)md5It.next();
                md5 = entry.getKey();
                ArrayList<String[]> info = entry.getValue();
                urlIt = info.iterator();
                while(urlIt.hasNext()){
                    String[] urlinfo = urlIt.next();
                    urlstr =urlinfo[1];
                    filesize = Long.valueOf(urlinfo[0]);




                    if(filesize == 0 || FileUtils.getSpace()<filesize) continue;

                    String[] urlstrs = new String[5];
                    urlstrs[0] = urlstr;
                    if(urlIt.hasNext()){
                        urlstrs[1]= urlIt.next()[1];
                    }else{
                        urlstrs[1]= urlstr;
                    }

                    if(urlIt.hasNext()){
                        urlstrs[2]= urlIt.next()[1];
                    }else{
                        urlstrs[2]= urlstr;
                    }

                    if(urlIt.hasNext()){
                        urlstrs[3]= urlIt.next()[1];
                    }else{
                        urlstrs[3]= urlstr;
                    }

                    if(urlIt.hasNext()){
                        urlstrs[4]= urlIt.next()[1];
                    }else{
                        urlstrs[4]= urlstr;
                    }

                    if(downloadByUrl(urlstrs,md5) != 1){
                        return -1;
                    }

                    while(!s_finished){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    };



                    if(buffer != null) {

                        if(md5 != null){

                            Log.d("download", "s7:");
                            //int len = buffer.length;
                            String checksum = fileUtils.md5sum(buffer);
                            if(!checksum.equals(md5)){
                                Log.d("download", "s8:");
                                allparts =null;
                                buffer = null;
                                return -1;
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
                        return 0;
                    }





                }

            }
            return -1;

        }




















/*
        if (FileUtils.isFileExist(path, fileName)){
            int progressive =  current +  thisstep;
            publishProgress(progressive);
            return 1;
        }else{
            if(filesize == 0) return -1;
            if(FileUtils.getSpace()<filesize) return -1;


            Log.i("download", "s3:");
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
            int trys = 3;


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
                    block = 1024;
                    break;
                } else
                    trys--;

            }
            map = null;

            Log.d("download", "s4:"+String.valueOf(trys));
            if(trys<=0)
                return 0;
            final String url = urlstr;

            int count=1;
            if (fz/block!=0) {
                steps = (int)(fz/block)+1;
            }else{
                steps = (int)(fz/block);
            }

            buffer = new byte[(int)fz];
            allparts = new HashMap<Integer, Boolean>();

            s_finished = false;
            from =0;
            pool = Executors.newFixedThreadPool(3);

            for(int thnum=0;thnum<steps;thnum++){
                final int idx = thnum;
                if(fz-1>=from+block-1){
                    pool.execute(new MyTask(url,from,block,idx));
                    from += block;
                }else{
                    Thread t = new Thread(new MyTask(url,from,fz-from,idx));
                    t.start();
                    pool.execute(new MyTask(url, from, fz - from, idx));
                    from = fz;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while(!s_finished){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

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
                return 0;
            }
        }*/

    }
    public int downloadByUrl(String[] urlstr,String md)
    {
        //Log.i("download", "s3:");
        //curr = current;
        //steplengh = thisstep;
        times = 100;
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

        //long start = 0;
        int trys = 3;

        HashMap<String,Long> map = new HashMap<String, Long>();
        while(trys>0) {

            if(isCancelled()) {
                map =null;
                allparts =null;
                buffer = null;
                return -1;
            }

            int re = getFileSize(urlstr[3-trys], map);

            if (re == 0) {
                fz = map.get("size");
                block = fz;
                break;
            } else if (re == 1) {
                fz = map.get("size");
                block = 1024*100;
                break;
            } else
                trys--;

        }
        map = null;

        //Log.d("download", "s4:"+String.valueOf(trys));
        if(trys<=0)
            return 0;
        //final String url = urlstr;

        int count=1;
        if (fz%block!=0) {
            steps = (int)(fz/block)+1;
        }else{
            steps = (int)(fz/block);
        }

        buffer = new byte[(int)fz];
        allparts = new HashMap<Integer, Boolean>();

        s_finished = false;
        from =0;
        pool = Executors.newFixedThreadPool(10);

        for(int thnum=0;thnum<steps;thnum++){
            final int idx = thnum;
            if(fz-1>=from+block-1){
                pool.execute(new MyTask(urlstr[thnum%5],from,block,idx,md));
                from += block;
            }else{
                pool.execute(new MyTask(urlstr[thnum%5], from, fz - from, idx,md));
                from = fz;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        return 1;

    }
    public void downloadByThread(String url,long start,long size,int index,String md)
    {
        if(isCancelled()) {
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.obj = null;
            msg.sendToTarget();
            return;
        }

        InputStream inputStream=getInputStreamFormUrl(url,start,start+size-1);

        int res = fileUtils.writeToArrayfromInput(inputStream,buffer,start,size);
        if(res==0){
            //handler.sendEmptyMessage(index);
            String[] urlinfo = {"",md};
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.arg1 = (int)start;
            msg.arg2 = (int)size;
            msg.obj = urlinfo;
            msg.sendToTarget();
        }else{
            String[] urlinfo = {url,md};
            Message msg = handler.obtainMessage();
            msg.what = index;
            msg.arg1 = (int)start;
            msg.arg2 = (int)size;
            msg.obj = urlinfo;
            msg.sendToTarget();
/*
            from +=block;
            to +=block;
            int progressive =  current +  (thisstep*count)/step;
            publishProgress(progressive);
            count++;*/

        }
    }

    private Handler handler;
    /**
     * 根据URL得到输入流
     * @param urlstr
     * @return
     */
    public InputStream getInputStreamFormUrl(String urlstr,long start,long end){
        InputStream inputStream=null;
        Log.d("mydownload", "threadstart:" + urlstr);

        try {
            URL url=new URL(urlstr);
            HttpURLConnection urlConn=(HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("Range","bytes="+start+"-"+end);
            urlConn.setConnectTimeout(1000000);
            urlConn.setReadTimeout(1000000);
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
        Log.d("mydownload", "threadend:" + String.valueOf(start) + ((inputStream == null) ? ":failed" : ":sucess"));

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