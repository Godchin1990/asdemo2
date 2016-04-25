package com.example.hellofacebook.http.http;


import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;


public class SingleDownLoadThread extends Thread {
    private float percent = 0;
    private URL url;
    private long fileLength;
    private ITransFileProcess iTransFileProcess;
    private String filePath;
    public static final String DOWNLOAD_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "download" + File.separator;

    public SingleDownLoadThread(URL url, ITransFileProcess iTransFileProcess) {
        this.url = url;
        this.iTransFileProcess = iTransFileProcess;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String fileID = UUID.randomUUID().toString();
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();    //建立一个远程连接句柄，此时尚未真正连接
            conn.setConnectTimeout(5 * 1000);    //设置连接超时时间为5秒
            conn.setRequestMethod("GET");    //设置请求方式为GET
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");    //设置用户代理
            conn.setRequestProperty("Connection", "Keep-Alive");    //设置Connection的方式
            //告诉对方（发送方）自己的地址,会以response的形式告知发送方
            conn.connect();    //和远程资源建立真正的连接，但尚无返回的数据流
            String field = conn.getHeaderField("Content-Disposition");
            String tmpStr1 = field.substring(field.indexOf("filename") + 10, field.length() - 1); //获取filename="xxx"中的xxx
            String realname = tmpStr1.substring(tmpStr1.lastIndexOf("/") + 1);
            filePath = DOWNLOAD_FOLDER + realname;
            fileLength = conn.getContentLength();
            byte[] buffer = new byte[8096];        //下载的缓冲池为8KB
            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            long downloadLength = 0;//当前已下载的文件大小
            int bufferLength = 0;
            while ((bufferLength = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bufferLength);
                bos.flush();
                //计算当前下载进度
                downloadLength += bufferLength;
                percent = downloadLength / (fileLength * 1.0f);
                iTransFileProcess.onUpload(percent * 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
