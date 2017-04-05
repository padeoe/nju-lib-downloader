package fix;

import spider.BookDownloader;

import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 错误恢复类。
 *
 * 用于对读取错误日志，进行错误恢复。
 * 错误主要包括页下载失败和书本下载失败两种。
 * @author padeoe
 * @Date 2017/1/11.
 */
public class Recovery {
    private String logLocation = Paths.get(System.getProperty("user.dir"), BookDownloader.ERROR_LOG_NAME).toString();
    private Pattern PageExceptionPattern = Pattern.compile("PageDLException\\{url='(.*)', location='(.*)'\\}");
    private Pattern BookExceptionPattern = Pattern.compile("Book\\{id='(.*)', name='(.*)', author='(.*)', publishDate='(.*)', theme='(.*)', bookClass='(.*)', detailBookClass='(.*)'\\}");
    private String bookRootLocation;
    public static final String FIX_LOG_FILENAME="fix.log";

    /**
     * 创建并初始化一个错误恢复对象。
     *
     * 指定错误日志文件的路径和书本下载的存储根路径
     * @param logLocation 错误日志文件的路径
     * @param bookRootLocation 书本下载存储路径的根分类路径
     */
    public Recovery(String logLocation, String bookRootLocation) {
        this.logLocation = logLocation;
        this.bookRootLocation = bookRootLocation;
    }

    /**
     * 读取错误日志，进行错误恢复
     */
    public void recover(){


    }

    /**
     * 设置页下载失败日志行的格式
     * @param pageExceptionPattern 页下载失败日志行的格式
     */
    public void setPageExceptionPattern(Pattern pageExceptionPattern) {
        PageExceptionPattern = pageExceptionPattern;
    }

    /**
     * 设置书本下载失败日志行的格式
     * @param bookExceptionPattern 书本下载失败日志行的格式
     */
    public void setBookExceptionPattern(Pattern bookExceptionPattern) {
        BookExceptionPattern = bookExceptionPattern;
    }

}
