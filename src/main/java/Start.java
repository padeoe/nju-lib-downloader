import object.Book;
import object.BookClass;
import object.Books;
import object.RootBookClass;
import spider.BookSearch;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author padeoe
 * @Date: 2016/12/10
 */
public class Start {
    /**
     * 一个使用示例。请修改下面代码的两个文件存储路径，再运行。
     * 当前示例会下载计算机分类下所有书。
     * 下载过程中可以终止程序从而终止下载。下一次下载时会跳过下载分类中已有的书本。
     *
     * @param args
     */
    public static void main(String[] args) {
        //创建一个书目分类，此处定义的是0T0P3010 计算机类，具体解释请参考中图法
        // 格式必须和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>一致
        BookClass root=new BookClass("0N","自然科学总论",new RootBookClass());
        try {
            root.downloadWithCataDir("F:\\Book\\all",5,"F:\\error.log");
            //      root.downloadWithCataDir("/opt/seafile/wkk_test/all",5,"/opt/seafile/wkk_test/error.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
