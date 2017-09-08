# NJU-lib-Downloader
[超星电子书](http://www.sslibrary.com/)和[书香中国](http://sxnju.chineseall.cn/home/index)的电子书下载器

命令行程序。可以下载图书并自动合成PDF。

## 依赖
* 建议环境 linux，windows环境可以使用ubuntu子系统
* [JDK9](http://jdk.java.net/9/)
* GhostScript ，PDFTK，QPDF。

```
apt-get install ghostscript
apt-get install pdftk
apt-get install qpdf
```
如果不安装 GhostScript ，PDFTK，QPDF，只有书香中国的图书会 PDF 合成失败。

## 使用方法
在 [release](https://github.com/padeoe/nju-lib-downloader/releases) 中下载发布的 jar 包，执行以下命令：
```
用法: java -jar libpdf.jar [options] <url>

其中选项包括:
   -t 线程数量
        默认为8。例如 -t 8
示例: java -jar libpdf.jar http://sxnju.chineseall.cn/v3/book/detail/VPeZj
      java -jar libpdf.jar http://img.sslibrary.com/n/slib/book/slib/10649113/65873989af6f4d809862aa11b16f650c/0e71a4d58ffba4e1b202d4b3fb30a81a.shtml?dxbaoku=false&deptid=275&fav=http%3A%2F%2Fwww.sslibrary.com%2Freader%2Fpdg%2Fpdgreader%3Fd%3Da1b248ecb4a78ba2087d8b5d0c5c950d%26ssid%3D10649113&fenlei=080401&spage=1&t=5&username=xxxxxx&view=-1
```
<h2>特别感谢</h2>
[@Nifury](https://github.com/Nifury)
