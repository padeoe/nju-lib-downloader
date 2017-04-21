package com.sslibrary.object;

/**
 * 根分类
 * <p>
 * 根分类是在中图法分类之外虚拟出的分类。
 * 用于集合管理所有子分类，以及作为起点，从服务器获取子分类。
 *
 * @author padeoe
 * @Date: 2016/12/20
 */
public class RootBookClass extends BookClass {
    public RootBookClass() {
        super("all");
    }

    /**
     * 用于判断{@link BookClass}对象是不是{@link RootBookClass}的实例
     *
     * @return true
     */
    @Override
    public boolean isRoot() {
        return true;
    }

    /**
     * 用于判断{@link BookClass}对象是不是{@link TerminalBookClass}的实例
     *
     * @return false
     */
    @Override
    public boolean isTerminal() {
        return false;
    }
}
