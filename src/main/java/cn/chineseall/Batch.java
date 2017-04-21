package cn.chineseall;

import com.sslibrary.spider.BookDownloader;

import java.io.IOException;
import java.lang.*;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Created by padeoe on 2017/4/14.
 */
public class Batch {
    public static void main(String[] args) {
        String className="TP";
        if(args!=null&&args.length>0){
            className=args[0];
        }
        try {
            Set<Book>books= new Class(className).getAllBooks();
            System.out.println(className+"分类共"+books.size()+"本书");
            String finalClassName = className;
            books.parallelStream()./*filter(book -> book.getAuthor().indexOf("(美")!=-1||book.getAuthor().indexOf("[美")!=-1).*/forEach(book -> {
                Downloader bookDownloader = new Downloader(book, new CoreService("testusername", "testusername"));
                bookDownloader.setPath(Paths.get("/mnt/f/"+ finalClassName));
                bookDownloader.setTmpPathDir(Paths.get("/mnt/f/tmp"));
                bookDownloader.setThreadNumber(2);
                if(!bookDownloader.downloadBook()){
                    BookDownloader.writeFile("/mnt/f/error.txt",book.getId()+" "+book.getName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
