package object;

import object.exception.BookDLException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spider.Controller;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * 图书
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
     * 初始化一个新创建的{@code Book}对象
     *
     * @param id 书本id，需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>服务器一致
     */
    public Book(String id) {
        this.id = id;
    }

    /**
     * 获取书本的id
     *
     * @return
     */
    public String getId() {
        return id;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取书本作者，可能是null
     *
     * @return
     */
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取书本出版日期
     *
     * @return
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
     * @return
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
    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    /**
     * 获取书本所在末级分类
     *
     * @return 字符串描述所属分类，最末层的分类，用“>”分割层级，
     * 例如“数理科学和化学图书馆>数学>总论复分>总论”
     */
    public String getDetailCatalog() {
        return detailCatalog;
    }

    public void setDetailCatalog(String detailCatalog) {
        this.detailCatalog = detailCatalog;
    }

    private String publishDate;
    private String theme;
    /**
     * 所属分类
     */
    Catalog catalog = new Catalog("all");
    /**
     * 所属分类，最末层的分类，字符串描述，“>”分割层级，
     * 例如“数理科学和化学图书馆>数学>总论复分>总论”
     */
    private String detailCatalog;

    public String getCookie() {
        return cookie;
    }

    void setCookie(String cookie) {
        this.cookie = cookie;
    }

    private String cookie;

    /**
     * 初始化一个新创建的{@code Book}对象。需要{@code Book}的所有属性。
     * 如果你没有足够的参数信息调用该方法创建对象。请调用{@link #Book(String)}
     *
     * @param id            {@code Book}的id。该id是服务器
     * @param name
     * @param author
     * @param publishDate
     * @param theme
     * @param catalog
     * @param detailCatalog
     */
    public Book(String id, String name, String author, String publishDate, String theme, Catalog catalog, String detailCatalog) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publishDate = publishDate;
        this.theme = theme;
        this.catalog = catalog;
        this.detailCatalog = detailCatalog;
    }


    /**
     * 通过在线阅览的地址来获取{@code Book}对象
     *
     * @param onlineReadUrl 书本的在线阅读地址
     * @return Book对象，仅指定了id
     */
    public static Book getBookFromUrl(String onlineReadUrl) {
        for (String para : onlineReadUrl.split("&")) {
            if (para.startsWith("ssnumber=")) {
                Book book= new Book(para.substring(9, para.length()));
                book.fillBookInfoByUrl(onlineReadUrl);
                return book;
            }
        }
        return null;
    }

    public static void main(String[] args) {
       Book book= Book.getBookFromUrl("http://114.212.7.104:8181/Jpath_sky/DsrPath.do?code=153BB79FEDBAFB093F90DDD4F90950EA&ssnumber=13488955&netuser=1&jpgreadmulu=1&displaystyle=0&channel=0&ipside=0");
    }

    public void fillBookInfoByUrl(String url){
        try {
            String html=new BookDownloader(this).getBookViewPageHtml(url);
            html=html.replaceAll("<!--","<");
            html=html.replaceAll("-->","");
            Document doc = Jsoup.parse(html);
            Elements nameNode=doc.getElementsByTag("title");
            this.name=nameNode.text();
            Elements infoNode=doc.getElementsByTag("span").not("[style]");
            for(Element node:infoNode){
                if(node.text().startsWith("作者：")){
                    this.author=node.text().substring(3,node.text().length());
                }
                if(node.text().startsWith("出版日期：")){
                    this.publishDate=node.text().substring(5,node.text().length());
                }
            }
        } catch (BookDLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取书本pdf的浏览地址。方法名和取自服务器js。
     *
     * @return 书本pdf在线观看的URL
     * @throws IOException IO错误
     */
    public String getbookread() throws IOException {
        resetCookie();
        String para = "BID=" + id + "&ReadMode=0&pdfread=0&displaystyle=0";
        String Url = Controller.baseUrl + "/getbookread?" + para;
        String result = MyHttpRequest.getWithCookie(Url, null, cookie, "UTF-8", 1000);
        return Controller.baseUrl + URLDecoder.decode(result);
    }

    private void resetCookie() throws IOException {
        cookie = (cookie == null) ? Controller.getSession() : cookie;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", theme='" + theme + '\'' +
                ", catalog=" + catalog.getId() +
                ", detailCatalog='" + detailCatalog + '\'' +
                '}';
    }
    /**
     * 下载该书。将下载许多图片，书的每一页都是一张png图片。
     * 将会在{@code pathname}下创建一个以书名命名的文件夹，并存储所有图片。
     * 错误日志将在当前路径下名为"error.log"
     */
    public void download() {
        BookDownloader bookDownloader = new BookDownloader(this);
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
    public void download(String pathname, int threadNumber) {
        BookDownloader bookDownloader = new BookDownloader(this);
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
    public void download(String pathname, int threadNumber, String errorLogPath) {
        BookDownloader bookDownloader = new BookDownloader(this);
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
}
