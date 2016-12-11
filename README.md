# NJU-lib-pdf-Downloader
用途？不可描述...
<h2>使用示例</h2>
按书本id下载
```
//创建Book对象，id=13544100，即《C# Primer Plus 中文版》一书
Book book = new Book("13544100");

//使用5个线程下载，保存到桌面
book.download("C:\\Users\\Username\\Desktop\\njulibpdf\\", 5);
```
按分类批量下载
```
//中图法分类 计算机软件类
Catalog root = new Catalog("0T0P3010");

//获取分类下所有书
List<Book> books = root.getAllBooks();

//下载该分类下所有书籍
books.forEach(book -> book.download("C:\\Users\\padeoe\\Desktop\\libpdf", 5));
```

分类浏览
```
//获取根分类
Catalog root = Catalog.getRootCatalog();

//加载二级分类
root.loadChild();

//输出每个分类id，名字，下属图书数量
root.getChildren().forEach(
        child -> System.out.println(
            child.getId() + " " + child.getName() + " " + child.getBooksSize()
            )
        );
```

错误恢复
```
new MissingPageCompletion("G:\\pageDLFail.txt").complete();//指定了错误日志路径
//或
new MissingPageCompletion().complete();//使用默认的错误日志路径
```
<h2>特别感谢</h2>
- [@Nifury](https://github.com/Nifury)
