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
### 直接使用jar包
在 release 中下载发布的 jar 包，执行以下命令：
```
java -jar libpdf.jar 网址
```
<h2>特别感谢</h2>
- [@Nifury](https://github.com/Nifury)
