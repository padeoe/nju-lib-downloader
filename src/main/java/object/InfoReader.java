package object;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * info文件解析器。
 * info文件是由{@link BookDownloader}在下载过程中创建的文本文件。
 * 记录了一个{@link Book#toString()}
 * 默认名称是{@link BookDownloader#INFO_FILE_NAME}。
 * 该类会读取info文件并解析出{@link Book}对象
 *
 * @author padeoe
 * @Date: 2016/12/11
 */
public class InfoReader {
    String infoFilePath;

    public InfoReader(String infoFilePath) {
        this.infoFilePath = infoFilePath;
    }

    /**
     * 解析{@code Book}对象，如果未找到返回null
     *
     * @return {@code Book}对象
     */
    public Book read() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(infoFilePath));
            String info = "";
            if (lines.size() > 0) {
                info = lines.get(0);
            }
            Pattern pattern = Pattern.compile("Book\\{id='(.*)', name='(.*)', author='(.*)', publishDate='(.*)', theme='(.*)', catalog=(.*), detailCatalog='(.*)'\\}");
            Matcher matcher = pattern.matcher(info);
            if (matcher.find()) {
                return new Book(matcher.group(1),
                        matcher.group(2),
                        matcher.group(3),
                        matcher.group(4),
                        matcher.group(5),
                        new Catalog(matcher.group(6)),
                        matcher.group(7));
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
