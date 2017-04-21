package cn.chineseall;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by padeoe on 2017/4/10.
 */
public class Book {
    String id;
    String name;
    String press;
    String author;
    String publishDate;
    String introduction;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Book(String id){
        this.id=id;
    }

    public Book(String id, String name, String press, String author, String publishDate, String introduction) {
        this.id = id;
        this.name = name;
        this.press = press;
        this.author = author;
        this.publishDate = publishDate;
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", press='" + press + '\'' +
                ", author='" + author + '\'' +
                ", publishDate='" + publishDate + '\'' +
                '}';
    }

    public List<Node> getOutline() throws IOException {
        for(int i=0;i<20;i++){
            try {
                String result=MyHttpRequest.get(CoreService.baseUrl+"/book/getDirectoryTree.jsps?bookId="+id+"&type=1",null,"UTF-8",3000);
                result=result.replaceAll("\\\\r","");
                result=result.replaceAll("\\\\n","");
                result=result.replaceAll("\\\\","");
                result=result.substring(result.indexOf("{\"data\":\"")+"{\"data\":\"".length(),result.indexOf("\",\"success\":true,\"msg\":\"\"}"));

                Document doc = Jsoup.parse(result);
                Elements elements=doc.select("ul[id=directoryTree]");
                return parseUL(elements.get(0));
            }catch (Exception e){
                if(i==19){
                    throw e;
                }
            }

        }
        return null;

    }

    public List<Node> parseUL(Element element){
        List<Node> nodes=new LinkedList<>();
        for(int i=0;i<element.children().size();i++){
            Element child=element.child(i);
            if(child.nodeName().equals("li")){
                nodes.add(parseLi(child));
            }
        }
        return nodes;
    }
    public Node parseLi(Element liElement){
        Elements children=liElement.children();
        if(children.size()==1&&children.get(0).nodeName().equals("a")){
            return parseA(children.get(0));
        }
        Node root=new Node();
        for(Element child:liElement.children()){
            if(child.nodeName().equals("span")){
                root=parseSpan(child);
            }
            if(child.nodeName().equals("ul")){
                root.addAll(parseUL(child));
            }
        }
        return root;
    }

    public Node parseSpan(Element spanElement){
        if(spanElement.children()!=null){
            Element trueNode=spanElement.child(0);
            return parseA(trueNode);
        }
        return new Node();
    }

    public Node parseA(Element aElement){
        Node result=new Node();
        result.setTitle(aElement.text());
        result.setPage(Integer.parseInt(aElement.attr("rel")));
        return result;
    }

    public static void main(String[] args) {

    }

}
