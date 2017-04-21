package cn.chineseall;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by padeoe on 2017/4/11.
 */
public class Node {
    private String title;
    private int page;
    private List<Node>children=new LinkedList<>();
    public void addChild(Node node){
        children.add(node);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node addAll(List<Node>nodes){
        nodes.forEach(node -> children.add(node));
        return this;
    }
}
