package spider;

import object.Book;
import object.Books;
import object.RootBookClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 查询符合条件的书籍。
 * <p>
 * 从<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>查询符合条件的书籍。
 * 可通过书名或者sql语句查询书籍。
 * 可以在查询过程中动态创建图书的分类目录结构。
 *
 * @author padeoe
 * @Date: 2016/12/09
 */
public class BookSearch {
    String cookie;

    /**
     * 查询
     *
     * @throws IOException 查询失败
     */
    public BookSearch() throws IOException {
        this.cookie = NJULib.getSession();
    }

    /**
     * 通过指定sql查询的where子句进行图书查询
     *
     * @param sqlWhereClause 一些已知字段包括"书名","主题词","出版日期","作者"
     * @param page           查询结果列表的页码
     * @param rootBookClass  查询到的书本将会添加进该分类结构
     * @return 查询结果，包含查询到的书本列表，书本总数量和结果总页数
     * @throws IOException 查询失败
     */
    public Books searchBySQL(String sqlWhereClause, int page, RootBookClass rootBookClass) throws IOException {
        String url = NJULib.baseUrl + "/markbook/BookSearch.jsp";
        String data = "Page=" + page + "&MethodType=1" + "&Library=&KeyName=0&Condition=" + URLEncoder.encode(sqlWhereClause, "UTF-8") + "&Sort=&links=0&PSize=10&_=";
        Map<String, String> requestProperty = new HashMap<>();
        requestProperty.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        String result = MyHttpRequest.postWithCookie(data, url, requestProperty, cookie, "UTF-8", "GBK", 2000);
        int totalNums = 0, totalPage = 0;
        Document doc = Jsoup.parse(result);
        Elements totalNumsNode = doc.select("input[name=TotalNums]");
        if (totalNumsNode != null && totalNumsNode.size() > 0) {
            totalNums = Integer.parseInt(totalNumsNode.get(0).attr("value"));
        }
        Elements totalPageNode = doc.select("a[href]:contains(末页)");
        if (totalPageNode != null && totalPageNode.size() > 0) {
            String href = totalPageNode.get(0).attr("href");
            int start = href.indexOf('(') + 1;
            int end = href.indexOf(')');
            if (start != 0 && end != -1) {
                totalPage = Integer.parseInt(href.substring(start, end));
            }
        }
        Set<Book> books = rootBookClass.queryBooks(result);
        return new Books(page, totalPage, totalNums, books);
    }

    /**
     * 通过指定sql查询的where子句进行图书查询
     *
     * @param sqlWhereClause where子句，一些已知字段包括"书名","主题词","出版日期","作者"
     * @param page           查询结果列表的页码
     * @return 如果没有匹配结果，返回空的对象
     * @throws IOException 查询失败
     */
    public Books searchBySQL(String sqlWhereClause, int page) throws IOException {
        return searchBySQL(sqlWhereClause, page, new RootBookClass());
    }

    /**
     * 通过指定sql查询的where子句进行图书查询，只返回第一页结果。
     *
     * @param sqlWhereClause where子句，一些已知字段包括"书名","主题词","出版日期","作者"
     * @return 如果没有匹配结果，返回空的对象
     * @throws IOException 查询失败
     */
    public Books searchBySQL(String sqlWhereClause) throws IOException {
        return searchBySQL(sqlWhereClause, 1);
    }

    /**
     * 通过指定sql查询的where子句进行图书查询
     *
     * @param sqlWhereClause where子句，一些已知字段包括"书名","主题词","出版日期","作者"
     * @return 查询结果，书的集合
     * @throws IOException 查询失败
     */
    public Set<Book> findAllBySQL(String sqlWhereClause) throws IOException {
        Set<Book> bookSet = null;
        Books firstPageBooks = searchBySQL(sqlWhereClause, 1);
        bookSet = firstPageBooks.getBookSet();
        for (int i = 2; i <= firstPageBooks.getTotalPage(); i++) {
            bookSet.addAll(searchBySQL(sqlWhereClause, i).getBookSet());
        }
        return bookSet;
    }

    /**
     * 通过指定sql查询的where子句进行图书查询,并把查询结果中的图书添加进分类结构
     *
     * @param sqlWhereClause where子句，一些已知字段包括"书名","主题词","出版日期","作者"
     * @param rootBookClass  根分类
     * @return 查询结果，书本集合
     * @throws IOException 查询失败
     */
    public Set<Book> findAllBySQL(String sqlWhereClause, RootBookClass rootBookClass) throws IOException {

        Books firstPageBooks = searchBySQL(sqlWhereClause, 1, rootBookClass);
        Set<Book> bookSet = firstPageBooks.getBookSet();
        for (int i = 2; i <= firstPageBooks.getTotalPage(); i++) {
            bookSet.addAll(searchBySQL(sqlWhereClause, i, rootBookClass).getBookSet());
        }
        return bookSet;
    }

    private Books searchByName(String name) throws IOException {
        return searchBySQL("书名 like '%" + name + "%' ");
    }
}
