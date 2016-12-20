package object;

/**
 * 根目录
 *
 * @author padeoe
 * @Date: 2016/12/20
 */
public class RootCatalog extends Catalog {
    public RootCatalog() {
        super("all");
    }

    /**
     * 用于判断{@link Catalog}对象是不是{@link RootCatalog}的实例
     *
     * @return true
     */
    @Override
    public boolean isRoot() {
        return true;
    }

    /**
     * 用于判断{@link Catalog}对象是不是{@link TerminalCatalog}的实例
     *
     * @return false
     */
    @Override
    public boolean isTerminal() {
        return false;
    }
}
