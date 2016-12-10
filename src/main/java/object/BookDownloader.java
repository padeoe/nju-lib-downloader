package object;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.network.MyHttpRequest;
import utils.network.ReturnData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    Map<PageType, Integer> pageNumberMap;
    String savePath = "G:\\Book\\";
    String directory;
    String urlPrefix;
    PageType[] pageTypes = {PageType.COVER, PageType.BOOKNAME, PageType.LEGALINFO, PageType.INTRODUCTION,
            PageType.DIRECTORY, PageType.CONTENT, PageType.APPENDIX, PageType.BACKCOVER};
    AtomicInteger needDownload = new AtomicInteger(1);

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public BookDownloader(Book book) {
        this.book = book;
    }

    public BookDownloader(String bookid) {
        this.book = new Book(bookid);
    }

    private static void download(String url, String pathname) throws IOException {
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
     * @param threadNumber
     * @throws IOException
     */
    public void downloadPng(int threadNumber) throws IOException, BookDLException {
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
        pageNumberMap = new HashMap<PageType, Integer>();
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
                    int n = 2 * Integer.parseInt(matcher.group(2)) - pageNumberMap.values().stream().mapToInt(number -> number.intValue()).sum();
                    pageNumberMap.put(pageTypes[5], n);
                }
            }
            String directoryName = book.getName() != null ? book.getName() : book.getId();
            directoryName = directoryName.replaceAll("[/\\\\:\"*?<>|]", " ");
            directory = savePath + directoryName + "\\";
            File path = new File(directory);
            boolean success = true;
            //目录不存在，创建目录
            if (!path.exists()) {
                success = path.mkdirs();
            } else {
                System.out.println(book.getName() + "已存在，跳过");
                return;
            }
            Vector<PageDLFailException> pageDLFailExceptions = new Vector<PageDLFailException>();
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

    public void downloadContent(int threadNumber) throws BookDLException {
        int firstPage = getFirstPage(PageType.CONTENT);//第一页的序号
        final int lastPage = firstPage + pageNumberMap.get(PageType.CONTENT) - 1;//最后一页序号
        //System.out.println("正文页码" + firstPage + "~" + lastPage);
        needDownload.set(firstPage);
        Vector<PageDLFailException> pageDLFailExceptions = new Vector<PageDLFailException>();
        ;
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
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
                                download(PageType.CONTENT, downloading, new StringBuilder().append(downloading).append(".png").toString());
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
     * @param pageType
     * @throws IOException
     */
    public void download(PageType pageType) throws BookDLException {
        Vector<PageDLFailException> pageDLFailExceptions = new Vector<PageDLFailException>();
        int base = getFirstPage(pageType);
        for (int i = 0; i < pageNumberMap.get(pageType); i++) {
            try {
                download(pageType, i + 1, new StringBuilder().append(base + i).append(".png").toString());
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
        for (int i = 0; i < pageTypes.length; i++) {
            if (pageTypes[i].equals(pageType)) {
                break;
            } else {
                base += pageNumberMap.get(pageTypes[i]);
            }
        }
        return base;
    }

    /**
     * 下载某一种页类型的特定页
     *
     * @param pageType
     * @param page
     * @throws IOException
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
        String pathname = directory + filename;
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
