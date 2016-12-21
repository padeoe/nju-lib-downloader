package fix;

import spider.BookDownloader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取下载日志中的错误，进行缺页补全。
 *
 * @author padeoe
 *         Date: 2016/12/09
 */
public class MissingPageCompletion {
    private String logLocation = Paths.get(System.getProperty("user.dir"), BookDownloader.ERROR_LOG_NAME).toString();
    private Pattern pattern = Pattern.compile("PageDLException\\{url='(.*)', location='(.*)'\\}");

    /**
     * 创建一个{@code MissingPageCompletion}对象并将日志路径指定为{@code logLocation}
     *
     * @param logLocation 日志文件路径
     */
    public MissingPageCompletion(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * 读取日志中所有下载失败的单页信息并重新下载一次。
     * 重新下载的日志会输入到原日志文件中
     */
    public void complete() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logLocation));
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                Matcher matcher = pattern.matcher(line);
                String url, location;
                if (matcher.find()) {
                    url = matcher.group(1);
                    location = matcher.group(2);
                    System.out.println(url + " " + location);
                    try {
                        BookDownloader.downloadImage(url, location);
                        iterator.remove();
                    } catch (IOException downloadFail) {
                    }
                }
            }

            StringBuilder newLog = new StringBuilder();
            lines.forEach(line -> newLog.append(line).append(System.getProperty("line.separator")));
            FileWriter writer = new FileWriter(logLocation, false);
            writer.write(newLog.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前指定的日志的位置。
     * 如果没有指定位置，将默认使用当前路径下的名为{@link BookDownloader#ERROR_LOG_NAME}的文件
     *
     * @return 当前指定的日志的位置
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * 指定输入的日志的位置
     *
     * @param logLocation 作为输入的日志的位置
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * 获取当前指定的错误日志的单行格式
     *
     * @return 错误日志的单行格式
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * 设置日志的单行格式
     *
     * @param pattern 日志的单行格式
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
