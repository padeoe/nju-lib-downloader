package com.sslibrary.spider;

import utils.network.MyHttpRequest;

import java.io.IOException;

/**
 * 用于获取Session
 *
 * @author padeoe
 *         Date: 2016/12/08
 */
public class NJULib {
    public static final String baseUrl = "http://114.212.7.104:8181";

    /**
     * 获取SeesionId
     *
     * @return SeesionId
     * @throws IOException 出现网络错误
     */
    public static String getSession() throws IOException {
        System.out.println("正在重置cookie");
        String Url = baseUrl + "/markbook/";
        return MyHttpRequest.getAndGetCookie(Url, null, "UTF-8", 1000)[1];
    }

}
