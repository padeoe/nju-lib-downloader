import com.sslibrary.spider.BookDownloader;
import picocli.CommandLine;

import java.nio.file.Paths;

/**
 * Created by padeoe on 2017/9/8.
 */
public class Starter implements Runnable {
    @CommandLine.Option(names = {"-t"}, description = "线程数量")
    private int threadNumber = 8;

    @CommandLine.Parameters(paramLabel = "URL", description = "书籍链接")
    private String url;

    @CommandLine.Option(names = {"-p", "--path"}, description = "pdf存储目录")
    private String outputPath;

    @CommandLine.Option(names = {"-c", "--cache_path"}, description = "临时文件（分页pdf）存储路径")
    private String tmpPath;

    public static void main(String[] args) {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        int exitCode = new CommandLine(new Starter()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {

        try {
            long begin = System.currentTimeMillis();
            if (url.contains("chineseall.cn")) {
                String[] segments = url.split("/");
                String bookId = segments[segments.length - 1];
                cn.chineseall.Downloader bookDownloader = new cn.chineseall.Downloader(bookId, new cn.chineseall.CoreService("Maskeney", "147258"));
                bookDownloader.setThreadNumber(threadNumber);
                if (tmpPath != null) bookDownloader.setTmpPathDir(Paths.get(tmpPath));
                if (outputPath != null) bookDownloader.setPath(Paths.get(outputPath));
                bookDownloader.downloadBook();
            } else {
                if (url.contains("img.sslibrary.com")) {
                    BookDownloader bookDownloader = new BookDownloader(url);
                    bookDownloader.setThreadNumber(threadNumber);
                    if (outputPath != null) bookDownloader.setPath(outputPath);
                    if (tmpPath != null) bookDownloader.setTmpPath(Paths.get(tmpPath));
                    bookDownloader.downloadBook();
                } else {
                    System.err.println("未能识别的url，请输入chineseall.cn或者img.sslibrary.com开头的书本url");
                }
            }
            System.out.println("下载结束，耗时" + (System.currentTimeMillis() - begin) / 1000 + "秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
