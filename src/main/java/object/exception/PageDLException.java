package object.exception;

/**
 * 下载图书的某一页时失败。该类包含了错误现场的信息，可用于错误恢复与后期处理
 *
 * @author padeoe
 *         Date: 2016/12/10
 */
public class PageDLException extends Exception {
    private String url;
    private String location;

    public PageDLException(String url, String location) {
        super();
        this.url = url;
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

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
