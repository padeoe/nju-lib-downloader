package object.exception;

import java.util.Vector;

/**
 * 下载某一本书时发生错误。
 *
 * 此异常发生在书本对应文件夹已经创建之后。
 * 包含了此书所有的书页下载错误{@code PageDLException}，用于错误恢复
 *
 * @author padeoe
 *         Date: 2016/12/10
 */
public class BookPagesDLException extends Exception {
    Vector<PageDLException> pageDLExceptions;

    /**
     * 构造一个{@code BookPagesDLException},用此书所有的书页下载错误初始化
     *
     * @param pageDLExceptionList 此书所有的书页下载错误
     */
    public BookPagesDLException(Vector<PageDLException> pageDLExceptionList) {
        this.pageDLExceptions = pageDLExceptionList;
    }

    /**
     * 获取页错误的集合
     *
     * @return 此书所有的书页下载错误{@code PageDLException}
     */
    public Vector<PageDLException> getPageDLExceptions() {
        return pageDLExceptions;
    }
}
