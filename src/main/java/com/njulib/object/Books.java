package com.njulib.object;

import java.util.Set;

/**
 * 书本查询的结果。{@link com.njulib.spider.BookSearch}类某些方法的返回值用到本类
 * 包含了查询出的图书当前页集合，以及查询结果的总页数，书本总数。
 *
 * @author padeoe
 * @Date: 2016/12/09
 */
public class Books {
    private int page;
    private int totalNums;
    private int totalPage;
    private Set<Book> bookSet;

    /**
     * @param page      当前页数
     * @param totalPage 总页数
     * @param totalNums 总书本数
     * @param bookSet   本页的书
     */
    public Books(int page, int totalPage, int totalNums, Set<Book> bookSet) {
        this.totalPage = totalPage;
        this.bookSet = bookSet;
    }

    /**
     * 获取查询到的图书总数
     *
     * @return 查询到的图书总数
     */
    public int getTotalNums() {
        return totalNums;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public Set<Book> getBookSet() {
        return bookSet;
    }
}
