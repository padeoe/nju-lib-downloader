package fix;

import object.BookDownloader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 缺页补全
 *
 * @author padeoe
 *         Date: 2016/12/09
 */
public class MissingPageCompletion {
    private String logLocation;
    private Pattern pattern = Pattern.compile("PageDLException\\{url='(.*)', location='(.*)'\\}");

    /**
     * 创建一个{@code MissingPageCompletion}对象并将日志路径指定为{@code logLocation}
     *
     * @param logLocation
     */
    public MissingPageCompletion(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * 创建一个{@code MissingPageCompletion}对象，
     * 将读取默认的错误日志路径，默认路径是当前路径下"error.log"
     */
    public MissingPageCompletion() {
        logLocation = Paths.get(System.getProperty("user.dir"), BookDownloader.ERROR_LOG_NAME).toString();
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
                        ;
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

    public String getLogLocation() {
        return logLocation;
    }

    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    public Pattern getPattern() {
        return pattern;
    }

    /**
     * 设置日志的单行格式
     *
     * @param pattern
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
