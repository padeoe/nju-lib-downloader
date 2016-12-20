package object;

import java.util.HashSet;
import java.util.Set;

/**
 * 终端目录。即目录的最末层。
 * 采用的是中图法分类，例如"哲学宗教>哲学理论>辩证唯物主义>总论"的最后一个"总论"就是一个终端目录。
 * 只有终端目录下可以直接附属图书。
 *
 * @author padeoe
 * @Date: 2016/12/20
 */
public class TerminalCatalog extends Catalog {
    private Set<Book> books = new HashSet<>();

    /**
     * 创建一个新初始化的{@code Catalog}对象，
     * 使之中图法分类标识是{@code id}
     *
     * @param id 目录的中图法分类标识。
     *           需要和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>定义的格式一致
     */
    public TerminalCatalog(String id) {
        super(id);
    }


    public TerminalCatalog(String id, String name, Catalog parent, boolean isLoaded) {
        super(id, name, parent, isLoaded);
    }

    /**
     * 获取目录下的书籍
     *
     * @return
     */
    public Set<Book> getBooks() {
        return books;
    }

    /**
     * 用于判断{@link Catalog}对象是不是{@link TerminalCatalog}的实例
     *
     * @return true
     */
    @Override
    public boolean isTerminal() {
        return true;
    }


    /**
     * 用于判断{@link Catalog}对象是不是{@link RootCatalog}的实例
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
     * @param book
     * @return
     */
    public boolean addBook(Book book) {
        return books.add(book);
    }
}
