package spider;

import utils.network.MyHttpRequest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <a href="http://114.212.7.104:8181/markbook/">南京大学馆藏数字化图书平台</a> 的pdf资源爬虫
 *
 * @author padeoe
 *         Date: 2016/12/08
 */
public class Controller {
    public static final String baseUrl = "http://114.212.7.104:8181";

    /**
     * 获取SeesionId
     *
     * @return SeesionId
     * @throws IOException 出现网络错误
     */
    public static String getSession() throws IOException {
        System.out.println("正在重置cookie");
        String Url = baseUrl + "/markbook/";
        return MyHttpRequest.getAndGetCookie(Url, null, "UTF-8", 1000)[1];
    }


    /**
     * 将url编码的unicode转成utf-8编码的字符串
     *
     * @param input 类似"%u7ecf%u5178%u7406%u8bba"的格式
     * @return
     */
    public static String decodeUrlUnicode(String input) {
        Pattern pattern = Pattern.compile("%u?([A-Za-z0-9]{2,4})");
        StringBuilder builder = new StringBuilder();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            builder.append((char) Integer.parseInt(matcher.group(1), 16));
        }
        return builder.toString();
    }
}
