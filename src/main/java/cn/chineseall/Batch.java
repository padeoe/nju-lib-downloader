package cn.chineseall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by padeoe on 2017/4/14.
 */
public class Batch {
    public static void main(String[] args) {
        String className="D9";
        if(args!=null&&args.length>0){
            className=args[0];
        }
        try {
            Stream<String> bookStream=new Class(className).getNewBooks().flatMap(books -> books.stream().map(Book::toString));
            //bookStream.forEach(book-> System.out.println(book));
            Files.write(Paths.get("C:\\Users\\padeo\\Desktop\\法律书籍.txt"), (Iterable<String>)bookStream::iterator);
/*            System.out.println(className+"分类共"+books.size()+"本书");
            String finalClassName = className;
            books.parallelStream().*//*filter(book -> book.getAuthor().indexOf("(美")!=-1||book.getAuthor().indexOf("[美")!=-1).*//*forEach(book -> {
                Downloader bookDownloader = new Downloader(book, new CoreService("", ""));
                bookDownloader.setPath(Paths.get("/mnt/f/"+ finalClassName));
                bookDownloader.setTmpPathDir(Paths.get("/mnt/f/tmp"));
                bookDownloader.setThreadNumber(2);
                if(!bookDownloader.downloadBook()){
                    BookDownloader.writeFile("/mnt/f/error.txt",book.getId()+" "+book.getName());
                }
            });*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
