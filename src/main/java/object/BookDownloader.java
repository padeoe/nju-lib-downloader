package object;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.network.MyHttpRequest;
import utils.network.ReturnData;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 书本的下载器，分离了下载相关的函数及变量
 *
 * @author padeoe
 *         Date: 2016/12/09
 */
public class BookDownloader {
    private Book book;
    private Map<PageType, Integer> pageNumberMap;
    private String savePath = System.getProperty("user.dir");
    private Path directory;
    private String urlPrefix;
    private PageType[] pageTypes = {PageType.COVER, PageType.BOOKNAME, PageType.LEGALINFO, PageType.INTRODUCTION,
            PageType.DIRECTORY, PageType.CONTENT, PageType.APPENDIX, PageType.BACKCOVER};
    private AtomicInteger needDownload = new AtomicInteger(1);
    public static final String ERROR_LOG_NAME = "error.log";

    void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    BookDownloader(Book book) {
        this.book = book;
    }

    BookDownloader(String bookid) {
        this.book = new Book(bookid);
    }

    public static void download(String url, String pathname) throws IOException {
        ReturnData returnData = MyHttpRequest.action_returnbyte("GET", null, url, null, null, null, 2000);
        byte[] a = returnData.getData();
        File file = new File(pathname);
        BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(file));
        bf.write(a, 0, a.length);
        bf.close();
    }

    /**
     * 下载书本书页的全部图片
     *
     * @param threadNumber 线程数
     * @throws IOException     因为网络问题下载未开始
     * @throws BookDLException 某些单页下载失败
     */
    void downloadPng_noLog(int threadNumber) throws IOException, BookDLException {
        String directoryName = book.getName() != null ? book.getName() : book.getId();
        directoryName = directoryName.replaceAll("[/\\\\:\"*?<>|]", " ");
        directory = Paths.get(savePath, directoryName);
        File path = directory.toFile();
        boolean success;
        //若目录不存在，创建目录
        if (path.exists()) {
            System.out.println("已存在，跳过" + book.toString());
            return;
        }
        success = path.mkdirs();
        //获取页面地址
        String url = book.getbookread();
        if (url == null || url.length() == 0) {
            System.out.println(book.getId() + " url获取失败");
            return;
        }
        //获取书本参数，包括下载地址前缀，页数
        String html = MyHttpRequest.get(url, null, "UTF-8", 2000);
        Document doc = Jsoup.parse(html);
        Element infoNode = doc.getElementsByTag("script").last();
        pageNumberMap = new HashMap<>();
        int epage = 0;
        if (infoNode.dataNodes().size() > 0) {
            String paraJs = infoNode.dataNodes().get(0).getWholeData();
            Pattern pattern = Pattern.compile("var str='(.*)';.*epage = (\\d+);.*pages :\\[\\[1,(\\d+)\\],\\[1,(\\d+)\\],\\[1,(\\d+)\\]," +
                    "\\[1,(\\d+)\\], \\[1,(\\d+)\\], \\[spage, epage\\], \\[1,(\\d+)\\], \\[1,(\\d+)\\]\\],.*", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(paraJs);
            while (matcher.find()) {
                for (int i = 1; i <= 9; i++) {
                    urlPrefix = matcher.group(1);
                    pageNumberMap.put(pageTypes[5], Integer.parseInt(matcher.group(2)));
                    pageNumberMap.put(pageTypes[0], Integer.parseInt(matcher.group(3)));
                    pageNumberMap.put(pageTypes[1], Integer.parseInt(matcher.group(4)));
                    pageNumberMap.put(pageTypes[2], Integer.parseInt(matcher.group(5)));
                    pageNumberMap.put(pageTypes[3], Integer.parseInt(matcher.group(6)));
                    pageNumberMap.put(pageTypes[4], Integer.parseInt(matcher.group(7)));
                    pageNumberMap.put(pageTypes[6], Integer.parseInt(matcher.group(8)));
                    pageNumberMap.put(pageTypes[7], Integer.parseInt(matcher.group(9)));
                    //修正正文页数服务器返回的实际是所有总页数的bug
                    int n = 2 * Integer.parseInt(matcher.group(2)) - pageNumberMap.values().stream().mapToInt(number -> number).sum();
                    pageNumberMap.put(pageTypes[5], n);
                }
            }


            Vector<PageDLFailException> pageDLFailExceptions = new Vector<>();
            if (success) {
                for (int i = 0; i < pageTypes.length; i++) {
                    if (i != 5) {
                        try {
                            download(pageTypes[i]);
                        } catch (BookDLException e) {
                            pageDLFailExceptions.addAll(e.getPageDLFailExceptions());
                        }
                    }
                }
                try {
                    downloadContent(threadNumber);
                    //记录书本信息
                    logBookInfo();
                } catch (BookDLException e) {
                    pageDLFailExceptions.addAll(e.getPageDLFailExceptions());
                }
                if (!pageDLFailExceptions.isEmpty()) {
                    throw new BookDLException(pageDLFailExceptions);
                }
            } else {
                System.out.println("文件夹创建失败");
            }

        } else {
            System.out.println(book.getId() + " 参数获取失败");
            return;
        }
    }

    void downloadPng(int threadNumber, String errorLogPath) {
        try {
            downloadPng_noLog(threadNumber);
        } catch (BookDLException e) {
            logPageFail(e, errorLogPath);//错误日志，记录单页下载失败
            e.getPageDLFailExceptions().forEach(pageDLException -> System.out.println(pageDLException));
        } catch (IOException io) {//书下载失败，全书没有开始下载
            logBookFail(errorLogPath);//错误日志，记录未下载书籍
        }
    }

    void downloadPng(int threadNumber) {
        downloadPng(threadNumber, ERROR_LOG_NAME);
    }

    /**
     * 在同文件夹下创建记录{@code Book}信息的文件，
     * 文件名是""info.txt""
     */
    private void logBookInfo() {
        try {
            FileWriter fileWriter = new FileWriter(new File(directory + "info.txt"));
            fileWriter.write(book.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadContent(int threadNumber) throws BookDLException {
        int firstPage = getFirstPage(PageType.CONTENT);//第一页的序号
        final int lastPage = firstPage + pageNumberMap.get(PageType.CONTENT) - 1;//最后一页序号
        //System.out.println("正文页码" + firstPage + "~" + lastPage);
        needDownload.set(firstPage);
        Vector<PageDLFailException> pageDLFailExceptions = new Vector<>();
        ArrayList<Thread> threadArrayList = new ArrayList<>();
        for (int i = 0; i < threadNumber; i++) {
            threadArrayList.add(new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        int downloading = needDownload.getAndIncrement();
                        if (downloading <= lastPage) {
                            //System.out.println("假装在下载 "+downloading);
                            try {
                                download(PageType.CONTENT, downloading, String.valueOf(downloading) + ".png");
                            } catch (PageDLFailException e) {
                                pageDLFailExceptions.add(e);
                            }
                        } else {
                            break;
                        }
                    }
                }
            });
        }
        for (Thread thread : threadArrayList) {
            thread.start();
        }
        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!pageDLFailExceptions.isEmpty()) {
            throw new BookDLException(pageDLFailExceptions);
        }
    }

    /**
     * 下载某一种页类型的所有页
     *
     * @param pageType 页类型
     * @throws BookDLException 某些页下载失败
     */
    private void download(PageType pageType) throws BookDLException {
        Vector<PageDLFailException> pageDLFailExceptions = new Vector<>();
        int base = getFirstPage(pageType);
        for (int i = 0; i < pageNumberMap.get(pageType); i++) {
            try {
                download(pageType, i + 1, String.valueOf(base + i) + ".png");
            } catch (PageDLFailException e) {
                pageDLFailExceptions.add(e);
            }
        }
        if (!pageDLFailExceptions.isEmpty()) {
            throw new BookDLException(pageDLFailExceptions);
        }
    }

    private int getFirstPage(PageType pageType) {
        int base = 1;//该种类型页的第一页的页码
        for (PageType pageType1 : pageTypes) {
            if (pageType1.equals(pageType)) {
                break;
            } else {
                base += pageNumberMap.get(pageType1);
            }
        }
        return base;
    }

    /**
     * 下载某一种页类型的特定页
     *
     * @param pageType 页类型
     * @param page     图书列表的第几页。分页是由服务器做出的
     * @throws PageDLFailException 某些页下载失败
     */
    private void download(PageType pageType, int page, String filename) throws PageDLFailException {
        int pageNumberLength = 6 - pageType.name.length();
        StringBuilder url = new StringBuilder();
        url.append(urlPrefix).append(pageType.name);
        for (int i = 0; i < pageNumberLength - String.valueOf(page).length(); i++) {
            url.append('0');
        }
        url.append(page);
        url.append(".jpg");
        String finalurl = url.toString();
        String pathname = directory.resolve(filename).toString();
        try {
            download(finalurl, pathname);
        } catch (IOException e) {
            try {
                download(finalurl, pathname);
            } catch (IOException e1) {
                throw new PageDLFailException(finalurl, pathname);
            }

        }
    }

    /**
     * 输出单页下载失败的日志，可以使用{@link spider.MissingPageCompletion}来读取错误日志并恢复
     *
     * @param bookDLException 单页失败异常
     * @param pageFailLogPath 日志路径
     */
    private void logPageFail(BookDLException bookDLException, String pageFailLogPath) {
        Vector<PageDLFailException> pageDLFailExceptions = bookDLException.getPageDLFailExceptions();
        for (PageDLFailException pageDLFailException : pageDLFailExceptions) {
            try {
                FileWriter writer = new FileWriter(pageFailLogPath, true);
                writer.write(pageDLFailException.toString());
                writer.write(System.getProperty("line.separator"));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 输出整本书下载失败的日志，用于后期恢复(暂未完成)
     *
     * @param bookFailLogPath 日志路径
     */
    private void logBookFail(String bookFailLogPath) {
        try {
            FileWriter writer = new FileWriter(bookFailLogPath, true);
            writer.write(book.getId() + " " + book.getName());
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 书页的类型。每本书都由"封面"，"正文"，"目录"等若干种固定的页类型组成。
     */
    enum PageType {
        COVER("cov", 1), BOOKNAME("bok", 2), LEGALINFO("leg", 3), INTRODUCTION("fow", 4), DIRECTORY("!", 5),
        CONTENT("", 6), APPENDIX("att", 7), BACKCOVER("cov", 8);
        private String name;
        private int index;

        PageType(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}
