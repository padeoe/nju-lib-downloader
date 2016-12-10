package object;

import java.util.Vector;

/**
 * 下载某一本书时发生错误。包含此书所有页的下载时产生的错误。用于错误恢复
 *
 * @author padeoe
 *         Date: 2016/12/10
 */
public class BookDLException extends Exception {
    Vector<PageDLFailException> pageDLFailExceptions;

    public BookDLException(Vector<PageDLFailException> pageDLFailExceptionList) {
        this.pageDLFailExceptions = pageDLFailExceptionList;
    }

    public Vector<PageDLFailException> getPageDLFailExceptions() {
        return pageDLFailExceptions;
    }
}
