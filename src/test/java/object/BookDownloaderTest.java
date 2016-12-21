package object;

import org.junit.Test;
import spider.BookDownloader;

/**
 * Created by padeoe on 2016/12/9.
 */
public class BookDownloaderTest {
    @Test
    public void download() throws Exception {
        new BookDownloader(new Book("11595586")).downloadAllImages();
    }

}