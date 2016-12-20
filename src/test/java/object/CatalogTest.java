package object;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by padeoe on 2016/12/9.
 */
public class CatalogTest {
    @Test
    public void getBooksSize() throws Exception {
        Catalog root = new RootCatalog();
        root.loadChild();
        int sum = 0;
        for (Catalog child : root.getChildren().values()) {
            sum += child.queryBooksSize();
        }
        assertTrue(sum >= 60465);
    }

}