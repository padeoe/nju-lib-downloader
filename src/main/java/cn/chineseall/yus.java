package cn.chineseall;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class yus {
    public static void main(String[] args) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://sxqh.chineseall.cn/v3/book/content/VPeZj/pdf/9").openConnection();
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", "JSESSIONID=6BC691FD580D2AFBCF38F4E9CB60FEC9");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        connection.connect();
        String location = connection.getHeaderField("Location");
        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.indexOf(';'));

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        try (InputStream is = connection.getInputStream()) {
            System.out.println(new String(is.readAllBytes()));
        }
    }
}