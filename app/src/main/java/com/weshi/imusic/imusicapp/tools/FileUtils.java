package com.weshi.imusic.imusicapp.tools;

/**
 * Created by apple28 on 15-8-5.
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

public class FileUtils {
    private static String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";

    public FileUtils(){
        //得到当前外部存储设备的目录
        SDPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     */
    public File createSDFile(String fileName){
        File file=new File(SDPath+fileName);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 在SD卡上创建目录
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName){
        File file=new File(SDPath+dirName);
        // 判断文件目录是否存在
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 判断SD卡上文件是否存在
     * @param fileName
     * @return
     */
    public static boolean isFileExist(String path,String fileName){
        File file=new File(SDPath+path+fileName);
        //file.delete();
        return file.exists();
    }

    /**
     * 将一个inputStream里面的数据写到SD卡中
     * @param path
     * @param fileName
     * @param inputStream
     * @return
     */
    public File writeToSDfromInput(String path,String fileName,InputStream inputStream,long size){
        //createSDDir(path);
        if(inputStream == null)
            return null;
        File file=createSDFile(path+fileName);
        OutputStream outStream=null;
        long readsz = 0;
        try {
            outStream=new FileOutputStream(file,true);
            byte[] buffer=new byte[1024];
            int numread=0;
            do{
                numread = inputStream.read(buffer);
                if(numread != -1) {
                    readsz += numread;
                    outStream.write(buffer, 0, numread);
                }
                else break;
            }while(true);
            /*
            while(inputStream.read(buffer)!=-1){
                outStream.write(buffer);
            }*/

            outStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                outStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(readsz == size)
            return file;
        else
            return null;


    }
    public int writeToArrayfromInput(InputStream inputStream,byte[] dst,long start,long size){
        //createSDDir(path);
        if(inputStream == null)
            return -1;
        //File file=createSDFile(path+fileName);
        //OutputStream outStream=null;
        long readsz = 0;
        try {
            //outStream=new FileOutputStream(file,true);
            //byte[] buffer=new byte[(int)size];
            //int numread=0;
            //do{
                //numread = ;
            //int num = inputStream.read(buffer);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytes = new byte[(int)size];

            int numRead = 0;
            while ((numRead = inputStream.read(bytes)) >= 0) {
                baos.write(bytes,0,numRead);
            }

            byte[] buffer = baos.toByteArray();
            int num = buffer.length;


            if(num == size) {
                    //readsz += numread;
                    //outStream.write(buffer, 0, numread);
                    System.arraycopy(buffer,0,dst,(int)start,(int)size);
                    inputStream.close();
                    return 0;
            }
            else{
                    inputStream.close();
                    return -1;
            }
                //else break;
            //}while(true);
            /*
            while(inputStream.read(buffer)!=-1){
                outStream.write(buffer);
            }*/

            //outStream.flush();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        //}finally{
            try {
                inputStream.close();
            } catch (IOException ee) {
                ee.printStackTrace();
                return -1;
            }
            return -1;
        }/*
        if(readsz == size)
            return file;
        else
            return null;*/


    }
    public File writeToSDfromData(String path,String fileName,byte[] buffer,long size){

        File file=createSDFile(path+fileName);
        OutputStream outStream=null;
        try {
            outStream=new FileOutputStream(file,true);
            outStream.write(buffer, 0, (int) size);
            outStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally{
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;


    }
    public static void getMediaFiles(Context ctx){
        File file = new File(FileUtils.getFilePath("imusic/", ""));

        if(file.exists() && file.isDirectory()){
            File[] array = file.listFiles();

            for(int i=0;i<array.length;i++){
                File f = array[i];

                if(f.isFile()){//FILE TYPE
                    String name = f.getName();

                    if(name.endsWith(".mp3")){
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uu = Uri.fromFile(f);
                        scanIntent.setData(uu);
                        ctx.sendBroadcast(scanIntent);
                    }
                }
            }
        }
    }

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F' };
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String md5sum(byte[] buffer) {

        MessageDigest md5;
        String resutl;
        try{
            md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer,0,buffer.length);
            resutl = toHexString(md5.digest());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resutl.toLowerCase();
    }


    public static  String getFilePath(String path,String fileName){
        return (SDPath+path+fileName);
    }



    public static  long getSpace(){
        StatFs st = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        int av = st.getAvailableBlocks();
        long bsize = st.getBlockSize();
        long spaceavailable = av*bsize;

        return spaceavailable;
    }
}