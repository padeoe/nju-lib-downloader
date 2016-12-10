import object.Book;
import object.BookDLException;
import object.Catalog;
import object.PageDLFailException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * @author padeoe
 *         Date: 2016/12/10
 */
public class Start {
    /**
     * 暂时作为入口点，请修改下面代码的三个文件存储路径，再运行。
     * 当前示例会下载计算机分类下4831本书。
     * 下载过程中可以终止程序从而终止下载。下一次下载时会跳过下载目录中已有的书本。
     *
     * @param args
     */
    public static void main(String[] args) {
        //创建一个书目分类，此处定义的是0T0P3010 计算机类，具体解释请参考中图法
        // 格式必须和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>一致
        //可以使用
        // Catalog root=Catalog.getRootCatalog();//获取根目录
        Catalog root = new Catalog("0T0P3010");
        try {
            List<Book> books = root.getAllBooks();
            for (Book book : books) {
                try {
                    book.download("G:\\Book\\", 5);//下载该分类下所有书
                } catch (BookDLException e) {
                    logPageFail(e, "G:\\未分类\\pageDLFail.txt");//错误日志，记录单页下载失败
                    e.getPageDLFailExceptions().forEach(pageDLException -> System.out.println(pageDLException));
                } catch (IOException io) {//书下载失败，全书没有开始下载
                    logBookFail(book, "G:\\未分类\\bookDLFail.txt");//错误日志，记录未下载书籍
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 输出单页下载失败的日志，用于后期恢复(暂未完成)
     *
     * @param bookDLException
     * @param pageFailLogPath
     */
    private static void logPageFail(BookDLException bookDLException, String pageFailLogPath) {
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
     * @param book
     * @param bookFailLogPath
     */
    private static void logBookFail(Book book, String bookFailLogPath) {
        try {
            FileWriter writer = new FileWriter(bookFailLogPath, true);
            writer.write(book.getId() + " " + book.getName());
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
