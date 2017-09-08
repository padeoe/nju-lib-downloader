import java.io.File;

/**
 * Created by padeo on 2017/9/8.
 */
public class Starter {
    public static void main(String[] args) {
        if(args!=null&&args.length>0){
            String url=args[args.length-1];
            int thread_index=-1;
            for(int i=0;i<args.length;i++){
                if(args[i].equals("-t")){
                    thread_index=i;
                    break;
                }
            }
            int threadNumber=(thread_index==-1)?8:Integer.parseInt(args[thread_index+1]);

            if(url.indexOf("chineseall.cn")!=-1){
                String[] segments = url.split("/");
                String bookId=segments[segments.length-1];
                cn.chineseall.Downloader bookDownloader = new cn.chineseall.Downloader(bookId, new cn.chineseall.CoreService("Maskeney", "147258"));
                bookDownloader.setThreadNumber(threadNumber);
                //   bookDownloader.setTmpPathDir(Paths.get("/mnt/f/tmp"));
                //  bookDownloader.setPath(Paths.get("/mnt/f/TP"));
                long begin = System.currentTimeMillis();
                bookDownloader.downloadBook();
                System.out.println((System.currentTimeMillis() - begin) / 1000);

            }
            else {
                if(url.indexOf("img.sslibrary.com")!=-1){
                    com.sslibrary.spider.BookDownloader bookDownloader = new com.sslibrary.spider.BookDownloader(url);
                    bookDownloader.downloadAllImages();
                    try {
                        new com.sslibrary.spider.PDFGenerator(bookDownloader.getDirectory().toFile(),new File("."),bookDownloader.getBook()).make();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("[ERROR] 未能识别的url，请输入chineseall.cn或者img.sslibrary.com开头的书本url");
                }
            }

        }
        else {
            System.out.println("用法: java -jar libpdf.jar [options] <url>");
            System.out.println("\n其中选项包括:");
            System.out.println("   -t 线程数量\n      \t默认为8。例如 -t 8");
            System.out.println("   <url>最好用引号括起来");
            System.out.println("示例: java -jar libpdf.jar http://sxnju.chineseall.cn/v3/book/detail/VPeZj");
            System.out.println("      java -jar libpdf.jar http://img.sslibrary.com/n/slib/book/slib/10649113/65873989af6f4d809862aa11b16f650c/0e71a4d58ffba4e1b202d4b3fb30a81a.shtml?dxbaoku=false&deptid=275&fav=http%3A%2F%2Fwww.sslibrary.com%2Freader%2Fpdg%2Fpdgreader%3Fd%3Da1b248ecb4a78ba2087d8b5d0c5c950d%26ssid%3D10649113&fenlei=080401&spage=1&t=5&username=xxxxxx&view=-1");
        }
    }
}
