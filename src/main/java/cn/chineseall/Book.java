package cn.chineseall;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.network.MyHttpRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by padeoe on 2017/4/10.
 */
public class Book {
    String id;
    String idInt;
    String name;
    String press;
    String author;
    String publishDate;
    String introduction;
    String coverUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdInt() {
        return idInt;
    }

    public void setIdInt(String idInt) {
        this.idInt = idInt;
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Book(String id){
        this.id=id;
    }

    public Book(String id, String name, String press, String author, String publishDate, String introduction, String coverUrl) {
        this.id = id;
        this.name = name;
        this.press = press;
        this.author = author;
        this.publishDate = publishDate;
        this.introduction = introduction;
        this.coverUrl = coverUrl;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", press='" + press + '\'' +
                ", author='" + author + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", introduction='" + introduction + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                '}';
    }

    public List<Node> getOutline() throws IOException {
        for(int i=0;i<20;i++){
            try {
                String url=CoreService.baseUrl+"/book/getDirectoryTree.jsps?bookId="+idInt+"&type=PDF";
                //http://sxnju.chineseall.cn/book/getDirectoryTree.jsps?bookId=10060602592&type=PDF&_=1504844448871
                String result= MyHttpRequest.get(url,null,"UTF-8",3000);
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

    public static List<Book>getBookFromHTML(String html){
        Document doc= Jsoup.parse(html);
        Elements infoNode=doc.select("div[class=boxListLi5]");
        List<Book>books=new ArrayList<>(30);
        if(infoNode!=null){
            for(int i=0;i<infoNode.size();i++){
                String id=null,name=null,author=null,publishDate=null,press=null,introduction=null,coverUrl=null;
                Elements idNameNode=infoNode.get(i).select("a[target=_blank][title]");
                if(idNameNode!=null&&idNameNode.size()>0){
                    Elements coverImageNode=infoNode.get(i).select("img[src]");
                    if(coverImageNode!=null&&coverImageNode.size()>0){
                        coverUrl=coverImageNode.attr("src");
                    }
                    name=idNameNode.get(0).attr("title");
                    id=idNameNode.get(0).attr("href");
                    if(id.indexOf("/book/")!=-1){
                        id=id.substring(6,id.length());
                    }
                    Elements pressNode=infoNode.get(i).select("span");
                    if(pressNode!=null&&pressNode.size()>0){
                        String pressInfo=pressNode.get(0).text();
                        if(pressInfo!=null){
                            String[]pressInfoArray=pressInfo.split("/");
                            if(pressInfoArray!=null&&pressInfoArray.length==3){
                                author=pressInfoArray[0].trim();
                                press=pressInfoArray[1].trim();
                                publishDate=pressInfoArray[2].trim();
                            }
                        }
                    }
                    Elements introNode=infoNode.get(i).select("p");
                    if(introNode!=null&&introNode.size()>0){
                        introduction=introNode.text();
                    }
                }
                if(id!=null){
                    Book book=new Book(id,name,press,author,publishDate,introduction,coverUrl);
                    books.add(book);
                    System.out.println(book);
                }
            }
        }
        return books;
    }


    public static void main(String[] args) {
/*        try {
            new Class("TP").getBooks(1);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
