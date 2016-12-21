package object;

import org.junit.Test;
import spider.NJULib;

import static org.junit.Assert.assertTrue;

/**
 * Created by padeoe on 2016/12/9.
 */
public class BookTest {
    @Test
    public void getbookread() throws Exception {
        String expected = NJULib.baseUrl + "/Jpath_sky/DsrPath.do?code=E3589DF005B0E05BBD3DC4AB57B92748&ssnumber=11595586&netuser=1&jpgreadmulu=1&displaystyle=0&channel=0&ipside=0";
        String result = new Book("11595586").getbookread();
        System.out.println(result);
        assertTrue(result.equals(expected));
    }
}