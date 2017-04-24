package com.njulib.object;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.njulib.spider.BookDownloader;
import com.njulib.spider.NJULib;
import utils.conversion.MyDecoder;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 图书的分类。
 * <p>
 * 对应<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a> 中的图书。
 * 同时分类名和分类编号满足中图法分类。是树结构。具有查询子分类和查询分类下书籍列表，批量下载分类书籍等功能。
 * 如果你没有足够信息构造实例，可以通过{@link RootBookClass}查询所有分类来获取实例。
 * 或者{@link com.njulib.spider.BookSearch}中的一些方法获取实例。
 *
 * @author padeoe
 * @Date: 2016/12/08
 */
public class BookClass {
    /**
     * 分类id，服务器定义的中图法分类id，
     * 例如"0T0P3010"
     */
    private String id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父分类
     */
    private BookClass parent;
    /**
     * 子分类列表
     */
    private Map<String, BookClass> children;


    /**
     * 子分类{@link #children}是否已经被加载
     */
    private boolean isLoaded = false;

    /**
     * 查看当对象所使用的cookie
     *
     * @return cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * 设置{@code cookie},BookClass的子每一次子分类加载，
     * 书籍查询等操作都需要cookie，设置的cookie将会对所有子分类使用，
     * 以避免频繁获取cookie
     *
     * @param cookie cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * 查询分类信息时向服务器发送的cookie，初始=null。
     * 当调用了需要网络的方法时，将会被初始化。
     * 一个{@link BookClass}对象的所有子分类{@link #children}都是用的同一个cookie
     */
    private String cookie;

    /**
     * 获取子分类的数量
     *
     * @return 子分类的数量
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * 获取父分类
     *
     * @return 父分类。如果不存在则为null
     */
    public BookClass getParent() {
        return parent;
    }


    /**
     * 获取所有子分类。
     * 初始为null,若要查看子分类，必须先调用{@link #loadChild()}或者{@link #loadAllChild()}从服务器查询并加载
     *
     * @return 子分类的集合
     */
    public Set<BookClass> getChildren() {
        return children.values().stream().collect(Collectors.toSet());
    }

    /**
     * 查询特定子分类。
     *
     * @param idOrName 子分类的名称或者代号。符合中图法分类。
     * @return 子分类
     */
    public BookClass getChild(String idOrName) {
        return children.get(idOrName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParent(BookClass parent) {
        this.parent = parent;
    }

    public boolean isTerminal() {
        return false;
    }

    /**
     * 添加一个子分类
     *
     * @param bookClass 子分类
     * @return 如果同id的子分类已存在，则返回之前的子分类，如果不存在，则添加并返回null
     */
    public BookClass addChild(BookClass bookClass) {
        if (bookClass.name != null) {
            children.putIfAbsent(bookClass.name, bookClass);
        }
        return children.putIfAbsent(bookClass.id, bookClass);
    }

    /**
     * 创建并初始化一个书本分类。指定分类编号，分类名称和父分类。
     *
     * @param id     分类编号
     * @param name   分类名称
     * @param parent 父分类
     */
    public BookClass(String id, String name, BookClass parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        children = new HashMap<>();
    }

    /**
     * 创建一个新初始化的{@code BookClass}对象，
     * 使之中图法分类标识是{@code id}
     *
     * @param id 分类的中图法分类标识。
     *           需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>定义的格式一致
     */
    public BookClass(String id) {
        this.id = id;
        children = new HashMap<>();
        this.isLoaded = false;
    }

    /**
     * 加载子分类。仅加载一层子分类，即子分类的子分类不会被加载。
     * 当该方法被调用时，会向服务器查询该分类的子分类并更新该对象的{@link #children}
     * <p>
     * 如需递归加载子分类，调用{@link #loadAllChild()}
     *
     * @throws IOException 从服务器查询子节点出错
     */
    public void loadChild() throws IOException {
        if (!isTerminal()) {
            checkCookie();
            String Url = NJULib.baseUrl + "/classifyview";
            String data = "fenlei=" + this.getId() + "&lib=markbook";
            String result = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "UTF-8", 1000);
            // System.out.println(result);
            Document doc = Jsoup.parse(result);
            Elements li = doc.getElementsByTag("li");
            for (Element bookClassId : li) {
                String id = bookClassId.attr("id");
                String name = bookClassId.getElementsByTag("a").text();
                boolean hasSubTree = bookClassId.getElementsByTag("img").attr("onClick").contains("getSubTree");
                //System.out.println(id+" "+NJULib.decodeUrlUnicode(name));
                BookClass child = hasSubTree ? new BookClass(id, MyDecoder.decodeUrlUnicode(name), this) :
                        new TerminalBookClass(id, MyDecoder.decodeUrlUnicode(name), this);
                child.setCookie(cookie);
                this.addChild(child);
            }
            this.isLoaded = true;
        }
    }


    /**
     * 迭代加载所有子分类。
     * 直至加载到每个分类的末层分类。
     *
     * @throws IOException 从服务器查询时出错
     */
    public void loadAllChild() throws IOException {
        if (!isTerminal()) {
            loadChild();
            for (BookClass child : getChildren()) {
                child.loadAllChild();
            }
        }
    }


    /**
     * 下载分类下所有图书，会迭代测创建分类文件夹
     *
     * @param pathname     存储路径。将在该路径下创建多级分类目录并保存下载的图书
     * @param threadNumber 线程数
     * @param errorLogPath 错误日志路径
     * @throws IOException 连接失败的错误
     */
    public void downloadWithCataDir(String pathname, int threadNumber, String errorLogPath) throws IOException {
        if (!isTerminal()) {
            loadChild();
            for (BookClass child : getChildren()) {
                child.downloadWithCataDir(Paths.get(pathname, name == null ? id : name).toString(), threadNumber, errorLogPath);
            }
        } else {
            downloadAllBooks(Paths.get(pathname, name == null ? id : name).toString(), threadNumber, errorLogPath);
        }
    }

    /**
     * 下载分类下所有图书，会迭代测创建分类文件夹
     * 下载存储路径为当前路径，线程数为5，错误日志将保存在当前路径，文件名为{@link BookDownloader#ERROR_LOG_NAME}
     * 可以调用重载{@link #downloadWithCataDir(String, int, String)}设置参数
     *
     * @throws IOException 连接失败的错误
     */
    public void downloadWithCataDir() throws IOException {
        downloadWithCataDir(System.getProperty("user.dir"), 5, Paths.get(System.getProperty("user.dir"), BookDownloader.ERROR_LOG_NAME).toString());
    }

    /**
     * 从服务器获取该分类下图书列表的第{@code page}页。
     * 图书列表的分页是服务器做的，每页最多10条图书。
     * <p>
     * 页数的最大值可以根据{@link #queryBooksSize()}自行计算
     *
     * @param page 图书列表的页码
     * @return 列表该页记录的图书
     * @throws IOException 从服务器查询书本列表时出错
     */
    public Set<Book> queryBooks(int page) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=" + page + "&totalnumber=-1";
        String Url = NJULib.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Set<Book> books = queryBooks(html);
        return books;

    }

    /**
     * 获得某分类下的所有图书
     *
     * @return 分类下所有图书
     * @throws IOException 从服务器查询书本列表时出错
     */
    public Set<Book> queryAllBooks() throws IOException {
        return queryAllBooks(5);
    }

    /**
     * 获得分类下的所有图书
     *
     * @param threadNumber 线程数
     * @return 图书集合
     * @throws IOException 连接错误
     */
    public Set<Book> queryAllBooks(int threadNumber) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=1&totalnumber=-1";
        String Url = NJULib.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("a:contains(末页)");

        if (!form.isEmpty()) {
            String keyword = form.get(0).attr("href");
            String booksize = keyword.substring(keyword.lastIndexOf(",") + 1, keyword.length() - 1);
            int size = Integer.parseInt(booksize);
            System.out.println("一共 " + size + " 本书");
            Set<Book> books = queryBooks(html);
            List<PageGetThread> threadList = new ArrayList<>();

            AtomicInteger needGettedPage = new AtomicInteger(2);//需要获取的页码
            int lastPage = size / 10 + 1;//最后一页的页码
            //开始多线程刷所有页码
            for (int threadN = 0; threadN < threadNumber; threadN++) {
                threadList.add(new PageGetThread(needGettedPage, lastPage));
            }

            for (PageGetThread thread : threadList) {
                thread.start();
            }
            for (PageGetThread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threadList.forEach(pageGetThread -> books.addAll(pageGetThread.getThreadBooks()));
            return books;
        }
        return null;
    }

    /**
     * 下载分类下所有图书。
     * 所有书籍将直接保存在{@code pathname}目录下，每本书一个文件夹，以书名命名。如同名，则加作者名，如又同名，加书本编号
     *
     * @param pathname     存储路径。书本文件夹所在的上级路径
     * @param threadNumber 线程数
     * @param errorLogPath 错误日志路径
     * @throws IOException 连接失败的错误
     */
    public void downloadAllBooks(String pathname, int threadNumber, String errorLogPath) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=1&totalnumber=-1";
        String Url = NJULib.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("a:contains(末页)");
        if (!form.isEmpty()) {
            String keyword = form.get(0).attr("href");
            String booksize = keyword.substring(keyword.lastIndexOf(",") + 1, keyword.length() - 1);
            int size = Integer.parseInt(booksize);
            System.out.println(this.getPath()+"一共 " + size + " 本书");
            Set<Book> books = queryBooks(html);
            Set<Book> downloading;
            downloadBooks(books, pathname, threadNumber, errorLogPath);
            int lastPage = size / 10 + 1;//最后一页的页码
            int index = 1;
            for (int i = lastPage; i >= 2; i--) {
                downloading = queryBooks(i);
                for (Book book : downloading) {
                    if (books.add(book)) {
                        book.download(pathname, threadNumber, errorLogPath);
                        index++;
                    } else {
                        System.out.println("服务器返回了重复书籍，跳过 " + book);
                    }
                }
            }
            System.out.println("去重后共" + books.size() + "书，实际下载了" + (index + 10) + "本书(含失败)");
        }
    }

    private void downloadBooks(Set<Book> books, String pathname, int threadNumber, String errorLogPath) {
        for (Book book : books) {
            book.download(pathname, threadNumber, errorLogPath);
        }
    }


    /**
     * 获取所有图书列表的线程
     */
    class PageGetThread extends Thread {
        Set<Book> books = new HashSet<>();
        AtomicInteger needGettedPage;
        int lastPage;

        public PageGetThread(AtomicInteger needGettedPage, int lastPage) {
            this.needGettedPage = needGettedPage;
            this.lastPage = lastPage;
        }

        @Override
        public void run() {
            while (true) {
                int gettingpage = needGettedPage.getAndIncrement();
                if (gettingpage <= lastPage) {
                    try {
                        if (gettingpage % 10 == 0) {
                            resetCookie();
                        }
                        books.addAll(queryBooks(gettingpage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }

        public Set<Book> getThreadBooks() {
            return books;
        }
    }


    /**
     * 获取HTML文本中的书籍并根据其分类添加进当前的分类结构
     *
     * @param html 服务器特定页面返回的包含书本信息的HTML文本。
     *             服务器多个不同页面返回的包含书本信息的HTML中书本信息相关节点的格式都相似。均可调用本函数
     * @return HTML中记录的书本
     */
    public Set<Book> queryBooks(String html) {
        Document doc = Jsoup.parse(html);
        Elements booksliNode = doc.select("li[style]");
        return queryBooks(booksliNode);
    }

    private Set<Book> queryBooks(Elements booksliNode) {
        Set<Book> books = new HashSet<>();
        for (Element element : booksliNode) {
            //获取书名和id
            String name = null, id = null, author = null, publishDate = null, theme = null, detailBookClass = null;
            BookClass bookBookClass;
            Elements nameIdNode = element.select("p[class=name]");
            if (nameIdNode != null) {
                name = nameIdNode.text();
                Elements idNode = nameIdNode.select("a[onclick]");
                if (idNode != null && idNode.size() > 0) {
                    String idOnClick = idNode.get(0).attr("onclick");
                    int start = idOnClick.indexOf("(") + 1, end = idOnClick.lastIndexOf(",");
                    if (start != 0 && end != -1) {
                        id = idOnClick.substring(start, end);
                    }
                }
            }
            //获取分类
            BookClass[] bookClasses = new BookClass[0];
            Elements infoNode = element.select("p[class=info]");
            if (infoNode != null) {
                Elements bookInfos = infoNode.select("a");
                if (bookInfos != null && bookInfos.size() > 0) {
                    Element terminalCataNode = bookInfos.last();
                    bookInfos.remove(terminalCataNode);
                    List<BookClass> tmplist = bookInfos.stream()
                            .map(bookInfo -> getBookCata(bookInfo, false))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    BookClass terminalBookClass = getBookCata(terminalCataNode, true);
                    if (terminalBookClass != null) {
                        tmplist.add(terminalBookClass);
                    }
                    bookClasses = tmplist.toArray(bookClasses);
                }
            }
            bookBookClass = this.link(bookClasses);

            //获取作者，出版日期，主题词，分类
            String info = element.text();
            Pattern pattern = Pattern.compile("\\d+\\. (.*) 作者[:：](.*) 出版日期[:：](\\d+).*?(?:主题词[:：](.+))? 分类[:：](.*)");
            Matcher matcher = pattern.matcher(info);
            while (matcher.find()) {
                name = matcher.group(1);
                author = matcher.group(2);
                publishDate = matcher.group(3);
                theme = matcher.group(4);
                detailBookClass = matcher.group(5);
            }
            Pattern minPattern = Pattern.compile(".*(《.*》).*");
            Matcher minMatcher = minPattern.matcher(info);
            while (minMatcher.find()) {
                name = minMatcher.group(1);
            }

            //汇总书本
            if (name != null && id != null) {
                Book book = new Book(id, name, author, publishDate, theme, bookBookClass, detailBookClass);
                book.setCookie(cookie);
                books.add(book);
                if (bookBookClass.isTerminal()) {
                    ((TerminalBookClass) bookBookClass).addBook(book);
                } else {
                    System.out.println("未获取到分类信息，将不被归档 " + book);
                }
            } else {
                System.out.println("error: " + info);
            }
        }
        return books;
    }


    /**
     * 通过HTML中对应节点获取到书所在分类
     *
     * @param bookInfo   书本信息的HTML节点
     * @param isTerminal 是否是终端分类
     * @return 书所在分类。如果是终端分类将会返回{@code TerminalBookClass}
     */
    private BookClass getBookCata(Element bookInfo, boolean isTerminal) {
        String cataName = bookInfo.text();
        String href = bookInfo.attr("href");
        if (href != null) {
            int cataIdStart = href.indexOf('=') + 1;
            if (cataIdStart != 0) {
                String cataId = href.substring(href.indexOf('=') + 1, href.length());
                BookClass tmp = isTerminal ? new TerminalBookClass(cataId) : new BookClass(cataId);
                tmp.setName(cataName);
                return tmp;
            }

        }
        return null;
    }


    /**
     * 从服务器查询当前分类下图书的数量。包含所有子分类下的图书
     *
     * @return 当前分类下图书的数量
     * @throws IOException 查询失败
     */
    public int queryBooksSize() throws IOException {
        checkCookie();
        String data = "fenlei=" + this.getId() + "&mark=all&Page=1&totalnumber=-1";
        String Url = NJULib.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        // System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("input[name=totalnumber]");
        if (!form.isEmpty()) {
            String booksize = form.get(0).attr("value");
            return Integer.parseInt(booksize);
        }
        return 0;
    }


    /**
     * 检查{@code cookie}如果为null将会更新cookie
     *
     * @throws IOException 更新cookie失败
     */
    private void checkCookie() throws IOException {
        cookie = (cookie == null) ? NJULib.getSession() : cookie;
    }

    /**
     * 重置{@code cookie}
     *
     * @throws IOException 重置cookie失败
     */
    private void resetCookie() throws IOException {
        cookie = NJULib.getSession();
    }

    /**
     * 对当前分类添加子分类
     *
     * @param childBookClasses 顺次路径关系子分类，后一个是前一个的子分类。第一个是当前分类的子分类
     * @return 子分类的最后一级分类.若子路径参数为空，则为当前分类
     */
    public BookClass link(BookClass... childBookClasses) {
        BookClass currentBookClass = this;
        for (BookClass bookClass : childBookClasses) {
            BookClass previois = currentBookClass.addChild(bookClass);
            if (previois != null) {
                currentBookClass = previois;
            } else {
                bookClass.parent = currentBookClass;
                currentBookClass = bookClass;
            }
        }
        return currentBookClass;
    }

    /**
     * 获取分类对象所有终端分类下已存储的书籍
     * <p>
     * 不会触发网络请求，只是迭代收集子分类的下已存在的书籍。
     * 如要即时从服务器查询书籍，请调用{@link #queryAllBooks()}及其重载
     *
     * @return 该分类下属所有分类的图书集合
     */
    public Set<Book> getBooks() {
        return this.getChildren().stream().map(BookClass::getBooks).collect(HashSet::new, Set::addAll, Set::addAll);
    }


    /**
     * 判断两个{@code BookClass}是否是同一个分类。
     * 仅根据代号即{@link BookClass#id}来判断
     *
     * @param obj 任意对象
     * @return 对象是否是同一个分类
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BookClass))
            return false;
        if (obj == this)
            return true;
        return this.id.equals(((BookClass) obj).id);
    }

    /**
     * 获取分类所在的路径。
     * 返回可读的{@code String}，对二级分类到当前分类顺次所经路径分别调用{@link BookClass#toString()}，用"-"分割
     *
     * @return 从二级分类到当前分类顺次所经路径，用"-"分隔分类
     */
    public String getPath() {
        Stack<BookClass> parents = new Stack<>();
        BookClass bookClass = this;
        while (bookClass!=null&&!bookClass.isRoot()) {
            parents.push(bookClass);
            bookClass = bookClass.getParent();
        }
        StringBuilder sb = new StringBuilder();
        if (!parents.isEmpty()) {
            sb.append(parents.pop().toString());
        }
        while (!parents.isEmpty()) {
            sb.append("-");
            sb.append(parents.pop().toString());
        }
        return sb.toString();
    }

    /**
     * 用于判断{@link BookClass}对象是不是{@link RootBookClass}的实例
     *
     * @return 是否是根分类
     */
    public boolean isRoot() {
        return false;
    }

    /**
     * 返回{@code BookClass}的哈希值。
     * 会直接使用用{@link #id}的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * 返回{@code BookClass}的可读字符串描述。
     *
     * @return 格式是 "分类代号(分类名)"，如果分类名为null，则格式是"分类代号"
     */
    @Override
    public String toString() {
        return this.getId() + (this.getName() == null ? "" : "(" + this.getName() + ")");
    }
}
