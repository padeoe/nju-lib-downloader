package cn.chineseall;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.network.MyHttpRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cn.chineseall.Book.getBookFromHTML;

/**
 * Created by padeoe on 2017/4/11.
 */
public class Class {
    private AtomicInteger needGet = new AtomicInteger(1);
    private String id;
    public Class(String id){
        this.id=id;
    }
    public int getBookSize() throws IOException {
        String url= CoreService.baseUrl+"/org/show/sort/"+id+"/0";
        String result=MyHttpRequest.get(url,null,"UTF-8",3000);
        return getBookSizeFromHtml(result);
    }

    public static int getBookSizeFromHtml(String html){
        Document doc= Jsoup.parse(html);
        Elements sizeNode=doc.select("input[id=totalSize]");
        if(sizeNode!=null&&sizeNode.size()>0){
            String sizeString=sizeNode.attr("value");
            if(sizeString!=null){
                int sizeInt= Integer.parseInt(sizeString);
                return sizeInt;
            }
        }
        return -1;
    }
    public List<Book> getBooks(int page) {
        String url= CoreService.baseUrl+"/org/show/sort/"+id+"/"+page;
        String result= null;
        try {
            result = MyHttpRequest.get(url,null,"UTF-8",3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBookFromHTML(result);
    }

    public List<Book> getNewBooks(int page) {
        String url= CoreService.baseUrl+"/org/show/selfsort/150/"+page;
        String result= null;
        try {
            result = MyHttpRequest.get(url,null,"UTF-8",3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBookFromHTML(result);
    }

    public Stream<List<Book>> getNewBooks() throws IOException {
        int size= getBookSizeFromHtml(MyHttpRequest.get(CoreService.baseUrl+"/org/show/selfsort/150/0",null,"UTF-8",3000));
        int lastPage = size / 30 + 1;//最后一页的页码
        return IntStream.range(0,lastPage+1).parallel().mapToObj(page -> getNewBooks(page));
    }



    public Stream<List<Book>> getAllBooks() throws IOException {
        int size= getBookSize();
        int lastPage = size / 30 + 1;//最后一页的页码
        return IntStream.range(0,lastPage+1).parallel().mapToObj(page -> getBooks(page));
/*        int threadNumber=10;

            Set<Book> books = new HashSet<>();
            List<PageGetThread> threadList = new ArrayList<>();

            AtomicInteger needGettedPage = new AtomicInteger(0);//需要获取的页码

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
            return books;*/
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
                 //   try {
                     //  System.out.println("正在获取第"+gettingpage+"页");
                        books.addAll(getBooks(gettingpage));
/*                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                } else {
                    break;
                }
            }
        }

        public Set<Book> getThreadBooks() {
            return books;
        }
    }

    private static StringBuffer output = new StringBuffer("<html>\n" +
            "<head><meta charset='UTF-8'></head>" +
            "<table border=\"1\">\n" +
            "<tr>\n" +
            "  <th>编号</th>\n" +
            "  <th>书名</th>\n" +
            "  <th>作者</th>\n" +
            "  <th>出版年份</th>\n" +
            "  <th>出版社</th>\n" +
            "</tr>\n");

    private static String getBookLineInTable(Book book) {
        if (book != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<tr>\n");
            stringBuffer.append(getAttr(Book::getId, book));
            stringBuffer.append(getAttr(Book::getName, book));
            stringBuffer.append(getAttr(Book::getAuthor, book));
            stringBuffer.append(getAttr(Book::getPublishDate, book));
            stringBuffer.append(getAttr(Book::getPress, book));
            stringBuffer.append("</tr>");
            return stringBuffer.toString();
        } else {
            return null;
        }

    }

    private static String getAttr(Function<Book, String> attrGetter, Book book) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<td>");
        stringBuffer.append(attrGetter.apply(book));
        stringBuffer.append("</td>\n");
        return stringBuffer.toString();
    }
    public static void main(String[] args) {
/*        try {

            new Class("TP").getAllBooks().forEach(book -> output.append(getBookLineInTable(book)));
            output.append("</table>\n");
            output.append("</html>");
            FileWriter writer = null;
            try {
                writer = new FileWriter("TP.html", false);
                writer.write(output.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
