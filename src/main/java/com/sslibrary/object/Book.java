package com.sslibrary.object;

import cn.chineseall.Node;
import com.sslibrary.object.exception.BookDLException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.sslibrary.spider.BookDownloader;
import com.sslibrary.spider.NJULib;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 图书。
 * <p>
 * 对应<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a> 中的图书。
 *
 * @author padeoe
 * @Date: 2016/12/08
 */
public class Book {
    /**
     * 书的id,唯一识别号，是由<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>定义的
     */
    private String id;
    /**
     * 书名，应该总是包含书名号《》
     */
    private String name;
    private String author;
    /**
     * 书本出本社
     */
    private String press;

    private String outlineUrl;

    public String getOutlineUrl() {
        return outlineUrl;
    }

    public void setOutlineUrl(String outlineUrl) {
        this.outlineUrl = outlineUrl;
    }

    private List<Node> outline;

    public List<Node> getOutline() {
        return outline;
    }

    public void setOutline(List<Node> outline) {
        this.outline = outline;
    }

    /**
     * 初始化一个新创建的{@code Book}对象。
     * <p>
     * 如果你没有足够的参数信息调用该方法创建对象,可调用{@link #getBookFromUrl(String)}通过书本的在线阅读地址获取实例，
     * 或者使用{@link com.sslibrary.spider.BookSearch}中的方法根据书名等字段查询并创建满足条件的的图书实例。
     *
     * @param id 书本id，需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>服务器一致
     */
    public Book(String id) {
        this.id = id;
    }

    /**
     * 获取书本的编号
     *
     * @return 书本编号
     */
    public String getId() {
        return id;
    }

    /**
     * 设置书本编号
     *
     * @param id 书本编号
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取书本名
     *
     * @return 书名，包含书名号《》
     */
    public String getName() {
        return name;
    }

    /**
     * 设置书名
     *
     * @param name 书名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取书本作者，可能是null
     *
     * @return 书本作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置书本作者
     *
     * @param author 书本作者
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取书本出版社
     * @return
     */
    public String getPress() {
        return press;
    }

    /**
     * 指定书本出版社
     * @param press
     */
    public void setPress(String press) {
        this.press = press;
    }

    /**
     * 获取书本出版日期
     *
     * @return 书本出版日期
     */
    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * 获取书本主题词，可能是null
     *
     * @return 书本主题词
     */
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * 获取书本所在分类
     *
     * @return 书本所在分类
     */
    public BookClass getBookClass() {
        return bookClass;
    }

    public void setBookClass(BookClass bookClass) {
        this.bookClass = bookClass;
    }

    /**
     * 获取书本所在末级分类
     *
     * @return 字符串描述所属分类，最末层的分类，用&gt;分割层级，
     * 例如“数理科学和化学图书馆&gt;数学&gt;总论复分&gt;总论”
     */
    public String getDetailBookClass() {
        return detailBookClass;
    }

    public void setDetailBookClass(String detailBookClass) {
        this.detailBookClass = detailBookClass;
    }

    private String publishDate;
    private String theme;
    /**
     * 所属分类
     */
    private BookClass bookClass = new RootBookClass();
    /**
     * 所属分类的中文描述。
     * “>”分割层级，
     * 例如“数理科学和化学图书馆>数学>总论复分>总论”
     */
    private String detailBookClass;

    public String getCookie() {
        return cookie;
    }

    void setCookie(String cookie) {
        this.cookie = cookie;
    }

    private String cookie;

    /**
     * 初始化一个新创建的{@code Book}对象。需要{@code Book}的所有属性。
     * 如果你没有足够的参数信息调用该方法创建对象,可调用{@link #getBookFromUrl(String)}通过书本的在线阅读地址获取实例，
     * 或者使用{@link com.sslibrary.spider.BookSearch}中的方法根据书名等字段查询并创建满足条件的的图书实例。
     *
     * @param id              {@code Book}的id。该id是服务器命名的
     * @param name            书名
     * @param author          作者
     * @param publishDate     出版日期
     * @param theme           主题词
     * @param bookClass       书本分类
     * @param detailBookClass 书本分类分类名路径
     */
    public Book(String id, String name, String author, String publishDate, String theme, BookClass bookClass, String detailBookClass) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publishDate = publishDate;
        this.theme = theme;
        this.bookClass = bookClass;
        this.detailBookClass = detailBookClass;
    }


    /**
     * 通过在线阅览的地址来获取{@code Book}对象
     *
     * @param onlineReadUrl 书本的在线阅读地址
     * @return Book对象，仅指定了id
     */
    public static Book getBookFromUrl(String onlineReadUrl) {
        String[]para=onlineReadUrl.split("/");
        if(para!=null&&para.length>6){
            Book book=new Book(para[para.length-3]);
            book.fillBookInfoByUrl(onlineReadUrl);
            return book;
        }
        return null;
    }

    /**
     * 通过在线阅读页面补全{@code Book}的信息
     * 仅可补全{@link #name},{@link #id},{@link #author},{@link  #publishDate}
     *
     * @param url 书本的在线阅读页面
     */
    public void fillBookInfoByUrl(String url) {
        try {
            String html = new BookDownloader(this,url).getBookViewPageHtml();
            Document doc = Jsoup.parse(html);
            Elements nameNode = doc.getElementsByTag("title");
            this.name = nameNode.text().replaceAll("\\s+", " ");
            Elements infoNode = doc.select("div[id=bookinfo]");
            if(infoNode!=null){
                String bookinfo=infoNode.text();
                int author_end=bookinfo.indexOf(name);
                if(author_end!=-1){
                    this.author=infoNode.text().substring(0,author_end-1);
                }
                String [] bookInfo=infoNode.text().substring(author_end,infoNode.text().length()).split(",");
                if(bookInfo.length>2){
                    this.press=bookInfo[1];
                    this.publishDate=bookInfo[2];
                }
            }
        } catch (BookDLException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取书本的在线阅读地址。
     *
     * @return 书本在线与阅读的URL
     * @throws IOException IO错误
     */
    public String getbookread() throws IOException {
        resetCookie();
        String para = "BID=" + id + "&ReadMode=0&pdfread=0&displaystyle=0";
        String Url = NJULib.baseUrl + "/getbookread?" + para;
        String result = MyHttpRequest.getWithCookie(Url, null, cookie, "UTF-8", 1000);
        return NJULib.baseUrl + URLDecoder.decode(result, "UTF-8");
    }

    /**
     * 重置{@link #cookie}
     *
     * @throws IOException 重置cookie失败
     */
    private void resetCookie() throws IOException {
        cookie = (cookie == null) ? NJULib.getSession() : cookie;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", press='" + press + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", theme='" + theme + '\'' +
                ", bookClass=" + bookClass +
                ", detailBookClass='" + detailBookClass + '\'' +
                '}';
    }

    /**
     * 下载该书。将下载许多图片，书的每一页都是一张png图片。
     * 将会在{@code pathname}下创建一个以书名命名的文件夹，并存储所有图片。
     * 错误日志将在当前路径下名为"error.log"
     */
    public void download(String onlineReadUrl) {
        BookDownloader bookDownloader = new BookDownloader(this,onlineReadUrl);
        bookDownloader.downloadAllImages();
    }

    /**
     * 下载该书。将下载许多图片，书的每一页都是一张png图片。
     * 将会在{@code pathname}下创建一个以书名命名的文件夹，并存储所有图片。
     * 错误日志将在当前路径下名为"error.log"
     *
     * @param pathname     下载存储目录
     * @param threadNumber 下载线程数
     */
    public void download(String pathname, int threadNumber,String onlineReadUrl) {
        BookDownloader bookDownloader = new BookDownloader(this,onlineReadUrl);
        bookDownloader.setSavePath(pathname);
        bookDownloader.setThreadNumber(threadNumber);
        bookDownloader.downloadAllImages();
    }

    /**
     * 下载该书。将下载许多图片，书的每一页都是一张png图片。
     * 将会在{@code pathname}下创建一个以书名命名的文件夹，并存储所有图片。
     *
     * @param pathname     下载存储目录
     * @param threadNumber 线程数
     * @param errorLogPath 错误日志路径
     */
    public void download(String pathname, int threadNumber, String onlineReadUrl,String errorLogPath) {
        BookDownloader bookDownloader = new BookDownloader(this,onlineReadUrl);
        bookDownloader.setSavePath(pathname);
        bookDownloader.setThreadNumber(threadNumber);
        bookDownloader.setErrorLogPath(errorLogPath);
        bookDownloader.downloadAllImages();
    }


    @Override
    public int hashCode() {
        return Integer.parseInt(this.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Book))
            return false;
        if (obj == this)
            return true;
        return this.id.equals(((Book) obj).id);
    }

    public com.njulib.object.Book cast(){
        return new com.njulib.object.Book(id,name,author,publishDate,theme,null,null);
    }
}
