package com.example.hellofacebook.http.http;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;


public class SingleUpLoadThread extends Thread {
    private URL url;
    private ITransFileProcess iTransFileProcess;
    private File file;
    private Properties header;

    public SingleUpLoadThread(URL url, Properties header, File file, ITransFileProcess iTransFileProcess) {
        this.url = url;
        this.iTransFileProcess = iTransFileProcess;
        this.file = file;
        this.header = header;
    }

    @Override
    public void run() {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";

        try {
            HttpURLConnection con = null;
            con = (HttpURLConnection) url.openConnection();
            //下载需要将setDoInput方法的参数值设为true
            con.setDoInput(true);
            //上传需要将setDoOutput方法的参数值设为true
            con.setDoOutput(true);
            //禁止HttpURLConnection使用缓存
            con.setUseCaches(false);
            //使用POST请求，必须大写
            con.setRequestMethod("POST");
            con.setConnectTimeout(5 * 1000);
            //以下三行设置http请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");

            Enumeration e = header.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = header.getProperty(key);
                con.setRequestProperty(key, value);
            }

            //在模拟web页面向服务器端上传文件时，每个文件的开头需要有一个分界符，
            //分界符需要在http请求头中指定。boundary是任意一个字符串，一般为******
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            //上传文件相关信息，这些信息包括请求参数名，上传文件名，文件类型，但并不限于此
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=\"file1\";filename=\"" + file.getName() + "\"" + end);
            ds.writeBytes(end);
            //获得文件的输入流，通过流传输文件。在这里我重写了FileInputStream，为了监听上传进度
            CustomFileInputStream fStream = new CustomFileInputStream(file);
            fStream.setOnUploadListener(iTransFileProcess);
        /* 设置每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            // 从文件读取数据至缓冲区
            while ((length = fStream.read(buffer)) != -1) {
                //将资料写入DataOutputStream中
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

            fStream.close();
            ds.flush();
            //上传完成以后获取服务器的反馈
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
