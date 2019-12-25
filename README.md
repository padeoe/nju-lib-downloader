# NJU-lib-Downloader
[超星电子书](http://www.sslibrary.com/)和[书香中国](http://sxnju.chineseall.cn/home/index)的电子书下载器

命令行程序。可以下载图书并自动合成PDF。

## 依赖
* Java 8 +

## 使用方法
在 [release](https://github.com/padeoe/nju-lib-downloader/releases) 中下载发布的 jar 包，执行以下命令：
```
用法: java -jar libpdf.jar [-c=<tmpPath>] [-p=<outputPath>] [-t=<threadNumber>] URL
      URL                   书籍链接
  -c, --cache_path=<tmpPath>
                            临时文件（分页pdf）存储路径，默认为当前路径
  -p, --path=<outputPath>   pdf存储目录，默认为当前路径
  -t=<threadNumber>         线程数量，默认为8

示例: java -jar libpdf.jar -t 8 http://sxnju.chineseall.cn/v3/book/detail/VPeZj
      java -jar libpdf.jar -t 8 -p /home/pdf/ -c /tmp/pdf http://img.sslibrary.com/n/slib/book/slib/10649113/65873989af6f4d809862aa11b16f650c/0e71a4d58ffba4e1b202d4b3fb30a81a.shtml?dxbaoku=false&deptid=275&fav=http%3A%2F%2Fwww.sslibrary.com%2Freader%2Fpdg%2Fpdgreader%3Fd%3Da1b248ecb4a78ba2087d8b5d0c5c950d%26ssid%3D10649113&fenlei=080401&spage=1&t=5&username=xxxxxx&view=-1

```

### Docker

```
docker run --rm -v "$PWD":/ebook padeoe/nju-lib-downloader url
```

<h2>特别感谢</h2>

[@Nifury](https://github.com/Nifury)
