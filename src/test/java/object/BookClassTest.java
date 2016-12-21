package object;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by padeoe on 2016/12/9.
 */
public class BookClassTest {
    @Test
    public void getPath() throws Exception {
        BookClass root = new RootBookClass();
        System.out.println(root.link(new BookClass("001"), new BookClass("002")).getPath());
    }

    @Test
    public void getBooksSize() throws Exception {
        BookClass root = new RootBookClass();
        root.loadChild();
        int sum = 0;
        for (BookClass child : root.getChildren().values()) {
            sum += child.queryBooksSize();
        }
        assertTrue(sum >= 60465);
    }

}