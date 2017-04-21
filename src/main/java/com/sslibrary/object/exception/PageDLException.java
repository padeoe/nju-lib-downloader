package com.sslibrary.object.exception;

/**
 * 下载图书的某一页时失败。
 * <p>
 * 该类包含了错误现场的信息，可用于错误恢复与后期处理
 *
 * @author padeoe
 *         Date: 2016/12/10
 */
public class PageDLException extends Exception {
    private String url;
    private String location;

    /**
     * 创建并初始化一个{@code PageDLException}对象。指定下载地址和存储地址。
     *
     * @param url      出错页图片的网络地址
     * @param location 出错页图片本应存储的本地路径。不含图片后缀名
     */
    public PageDLException(String url, String location) {
        super();
        this.url = url;
        this.location = location;
    }

    /**
     * 获取出错页的URL
     *
     * @return 出错页的URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取出错页图片本应存储的本地路径。
     *
     * @return 出错页图片本应存储的本地路径。不含图片后缀名
     */
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "PageDLException{" +
                "url='" + url + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
