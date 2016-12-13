import object.Catalog;

import java.io.IOException;

/**
 * @author padeoe
 *         Date: 2016/12/10
 */
public class Start {
    /**
     * 暂时作为入口点，请修改下面代码的两个文件存储路径，再运行。
     * 当前示例会下载计算机分类下所有书。
     * 下载过程中可以终止程序从而终止下载。下一次下载时会跳过下载目录中已有的书本。
     *
     * @param args
     */
    public static void main(String[] args) {
        //创建一个书目分类，此处定义的是0T0P3010 计算机类，具体解释请参考中图法
        // 格式必须和<a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a>一致
        Catalog root = new Catalog("0T0P");
        try {
            root.downloadWithCataDir("G:\\", 5, "G:\\未分类\\pageDLFail.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
