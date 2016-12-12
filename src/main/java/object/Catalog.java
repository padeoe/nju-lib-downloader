package object;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spider.Controller;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图书的分类。是树结构。
 * 具有查询自分类和查询分类下书籍列表的功能。
 * 对象刚创建的时候是不具有子节点和父节点的，
 * 可以调用{@link #loadChild()}加载下一层子节点，
 * 或者调用{@link #loadAllChild()}迭代加载所有子节点。
 *
 * @author padeoe
 *         Date: 2016/12/08
 */
public class Catalog {
    /**
     * 目录id，服务器定义的中图法分类id，
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
    private Catalog parent;
    /**
     * 子分类列表
     */
    private List<Catalog> children;
    /**
     * 是否是子分类。用于区分根节点和普通节点，只有根节点是false
     */
    private boolean isLeaf;
    /**
     * 是否是末级分类
     */
    private boolean allowsChildren;
    /**
     * 子分类{@link #children}是否已经被加载
     */
    private boolean isLoaded = false;

    /**
     * 查看当对象所使用的cookie
     *
     * @return
     */
    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * 查询分类信息时向服务器发送的cookie，初始=null。
     * 当调用了需要网络的方法时，将会被初始化。
     * 一个{@link Catalog}对象的所有子分类{@link #children}都是用的同一个cookie
     */
    private String cookie;

    public int getChildCount() {
        return children.size();
    }

    public Catalog getParent() {
        return parent;
    }


    public boolean getAllowsChildren() {
        return allowsChildren;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * 获取所有子分类，初始为null。
     * 若要查看自分类，必须先调用{@link #loadChild()}或者{@link #loadAllChild()}从服务器查询并加载
     *
     * @return
     */
    public List<Catalog> getChildren() {
        return children;
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

    public void setParent(Catalog parent) {
        this.parent = parent;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public boolean isAllowsChildren() {
        return allowsChildren;
    }

    public void setAllowsChildren(boolean allowsChildren) {
        this.allowsChildren = allowsChildren;
    }

    public void addChild(Catalog catalog) {
        this.children.add(catalog);
    }

    private Catalog(String id, String name, Catalog parent, boolean isLeaf, boolean allowsChildren, boolean isLoaded) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        children = new ArrayList<Catalog>();
        this.isLeaf = isLeaf;
        this.allowsChildren = allowsChildren;
        this.isLoaded = isLoaded;
    }

    /**
     * 创建一个新初始化的{@code Catalog}对象，
     * 使之中图法分类标识是{@code id}
     *
     * @param id 目录的中图法分类标识。
     *           需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>定义的格式一致
     */
    public Catalog(String id) {
        new String("i");
        this.id = id;
        this.name = null;
        this.parent = null;
        children = new ArrayList<Catalog>();
        this.isLeaf = false;
        this.allowsChildren = true;
        this.isLoaded = false;
    }

    /**
     * 获取根目录。
     * 如果你没有足够的信息自己调用{@link #Catalog(String)}和
     * {@link #Catalog(String, String, Catalog, boolean, boolean, boolean)}创建{@code Catalog}对象，
     * 那么你应该使用该方法查看<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>服务器
     * 支持的所有分类
     *
     * @return
     */
    public static Catalog getRootCatalog() {
        Catalog root = new Catalog("all", "根目录", null, false, true, false);
        return root;
    }

    /**
     * 加载子分类。仅加载一层子分类，即子分类的子分类不会被加载。
     * 当该方法被调用时，会向服务器查询该分类的子分类并更新该对象的{@link #children}
     * <p>
     * 如需递归加载子分类，调用{@link #loadAllChild()}
     *
     * @return
     * @throws IOException
     */
    public Catalog loadChild() throws IOException {
        checkCookie();
        String Url = Controller.baseUrl + "/classifyview";
        String data = "fenlei=" + this.getId() + "&lib=markbook";
        String result = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "UTF-8", 1000);
        // System.out.println(result);
        Document doc = Jsoup.parse(result);
        Elements li = doc.getElementsByTag("li");
        for (Element catalogId : li) {
            String id = catalogId.attr("id");
            String name = catalogId.getElementsByTag("a").text();
            boolean hasSubTree = catalogId.getElementsByTag("img").attr("onClick").contains("getSubTree");
            //System.out.println(id+" "+Controller.decodeUrlUnicode(name));
            Catalog child = new Catalog(id, Controller.decodeUrlUnicode(name), this, true, hasSubTree, false);
            child.setCookie(cookie);
            this.addChild(child);
        }
        this.isLoaded = true;
        return this;
    }


    /**
     * 迭代加载所有子分类。
     * 直至加载到每个分类的末层分类。
     *
     * @throws IOException
     */
    public void loadAllChild() throws IOException {
        if (allowsChildren) {
            loadChild();
            for (Catalog child : getChildren()) {
                child.loadAllChild();
            }
        }
    }

    /**
     * 获取该分类下图书列表的第{@code page}页。
     * 图书列表的分页时服务器做的，每页最多10条图书。
     * <p>
     * 页数的最大值可以根据{@link #getBooksSize()}自行计算
     *
     * @param page
     * @return
     * @throws IOException
     */
    public Set<Book> getBooks(int page) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=" + page + "&totalnumber=-1";
        String Url = Controller.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Set<Book> books = getBooks(html);
        return books;

    }

    /**
     * 获得某分类下的所有图书
     *
     * @return 分类下所有图书
     * @throws IOException
     */
    public Set<Book> getAllBooks() throws IOException {
        return getAllBooks(5);
    }

    /**
     * 获得分类下的所有图书
     * @param threadNumber 线程数
     * @return 图书集合
     * @throws IOException 连接错误
     */
    public Set<Book> getAllBooks(int threadNumber) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=1&totalnumber=-1";
        String Url = Controller.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("a:contains(末页)");
        ;
        if (!form.isEmpty()) {
            String keyword = form.get(0).attr("href");
            String booksize = keyword.substring(keyword.lastIndexOf(",") + 1, keyword.length() - 1);
            int size = Integer.parseInt(booksize);
            System.out.println("一共 " + size + " 本书");
            Set<Book> books = getBooks(html);
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
     * 下载分类下所有图书
     * @param pathname 存储路径
     * @param threadNumber 线程数
     * @param errorLogPath 错误日志路径
     * @throws IOException 连接失败的错误
     */
    public void downloadAllBooks(String pathname, int threadNumber, String errorLogPath) throws IOException {
        checkCookie();
        String data = "fenlei=" + this.id + "&mark=all&Page=1&totalnumber=-1";
        String Url = Controller.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        //   System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("a:contains(末页)");
        if (!form.isEmpty()) {
            String keyword = form.get(0).attr("href");
            String booksize = keyword.substring(keyword.lastIndexOf(",") + 1, keyword.length() - 1);
            int size = Integer.parseInt(booksize);
            System.out.println("一共 " + size + " 本书");
            Set<Book> books = getBooks(html);
            Set<Book> downloading;
            downloadBooks(books, pathname, threadNumber, errorLogPath);
            int lastPage = size / 10 + 1;//最后一页的页码
            int index = 1;
            for (int i = lastPage; i >= 2; i--) {
                downloading = getBooks(i);
                for (Book book : downloading) {
                    if (books.add(book)) {
                        book.download(pathname, threadNumber, errorLogPath);
                        index++;
                    } else {
                        System.out.println("服务器返回了重复书籍，跳过 " + book);
                    }
                }
            }
            System.out.println("去重后共" + books.size() + "书");
            System.out.println("索引index记录的书本数是" + index);
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
                        //  System.out.println("页数"+gettingpage);
                        if (gettingpage % 10 == 0) {
                            resetCookie();
                        }
                        books.addAll(getBooks(gettingpage));
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


    private Set<Book> getBooks(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("li[style]");
        Set<Book> books = new HashSet<>();
        for (Element element : elements) {
            String name = null, id = null, author = null, publishDate = null, theme = null, detailCatalog = null;
            Elements nameNode = element.select("a[href=##]");
            if (!nameNode.isEmpty()) {
                String onclick = nameNode.get(0).attr("onclick");
                id = onclick.substring(onclick.indexOf("(") + 1, onclick.lastIndexOf(","));
            }
            String info = element.text();
            Pattern pattern = Pattern.compile("\\d+\\. (.*) 作者：(.*) 出版日期：(\\d+).*?(?:主题词：(.+))? 分类:(.*)");
            Matcher matcher = pattern.matcher(info);
            while (matcher.find()) {
                name = matcher.group(1);
                author = matcher.group(2);
                publishDate = matcher.group(3);
                theme = matcher.group(4);
                detailCatalog = matcher.group(5);
            }
            Pattern minPattern = Pattern.compile(".*(《.*》).*");
            Matcher minMatcher = minPattern.matcher(info);
            while (minMatcher.find()) {
                name = minMatcher.group(1);
            }
            if (name != null && id != null) {
                Book book = new Book(id, name, author, publishDate, theme, this, detailCatalog);
                book.setCookie(cookie);
                books.add(book);
            } else {
                System.out.println("error: " + info);
            }
        }
        return books;
    }


    /**
     * 查询当前分类下图书的数量。包含所有子分类下的图书
     *
     * @return 当前分类下图书的数量
     * @throws IOException
     */
    public int getBooksSize() throws IOException {
        checkCookie();
        String data = "fenlei=" + this.getId() + "&mark=all&Page=1&totalnumber=-1";
        String Url = Controller.baseUrl + "/markbook/classifybookview.jsp";
        String html = MyHttpRequest.postWithCookie(data, Url, null, cookie, "UTF-8", "GBK", 1000);
        // System.out.println(html);
        Document doc = Jsoup.parse(html);
        Elements form = doc.select("a:contains(末页)");
        ;
        if (!form.isEmpty()) {
            String keyword = form.get(0).attr("href");
            String booksize = keyword.substring(keyword.lastIndexOf(",") + 1, keyword.length() - 1);
            return Integer.parseInt(booksize);
        }
        return 0;
    }


    private void checkCookie() throws IOException {
        cookie = (cookie == null) ? Controller.getSession() : cookie;
    }

    private void resetCookie() throws IOException {
        cookie = Controller.getSession();
        System.out.println(cookie);
    }


}
