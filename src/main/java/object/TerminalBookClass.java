package object;

import java.util.HashSet;
import java.util.Set;

/**
 * 终端分类。即分类的最末层。
 * <p>
 * 采用的是中图法分类，例如"哲学宗教-哲学理论-辩证唯物主义-总论"的最后一个"总论"就是一个终端分类。
 * 只有终端分类下可以存储图书。
 *
 * @author padeoe
 * @Date: 2016/12/20
 */
public class TerminalBookClass extends BookClass {
    private Set<Book> books = new HashSet<>();

    /**
     * 创建一个新初始化的{@code BookClass}对象，
     * 使之中图法分类标识是{@code id}
     *
     * @param id 分类的中图法分类标识。
     *           需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>定义的格式一致
     */
    public TerminalBookClass(String id) {
        super(id);
    }


    /**
     * 构造函数。
     *
     * @param id     分类编号
     * @param name   分类名
     * @param parent 父分类
     */
    public TerminalBookClass(String id, String name, BookClass parent) {
        super(id, name, parent);
    }

    /**
     * 获取分类下的书籍
     * 该方法只是返回该分类下现有书籍，不会向服务器查询该分类下所有图书。
     * 如需向服务器查询，请调用{@link BookClass#queryAllBooks()}及其重载方法
     *
     * @return 分类下的书籍。
     */
    public Set<Book> getBooks() {
        return books;
    }

    /**
     * 用于判断{@link BookClass}对象是不是{@link TerminalBookClass}的实例
     *
     * @return true
     */
    @Override
    public boolean isTerminal() {
        return true;
    }


    /**
     * 用于判断{@link BookClass}对象是不是{@link RootBookClass}的实例
     *
     * @return false
     */
    @Override
    public boolean isRoot() {
        return false;
    }

    /**
     * 增加分类下图书
     *
     * @param book 图书
     * @return 如果分类下已有该图书，将返回false。如果没有，将添加并返回true
     */
    public boolean addBook(Book book) {
        return books.add(book);
    }
}
