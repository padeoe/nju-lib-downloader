package utils.conversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author padeoe
 * @Date 2016/12/21
 */
public class MyDecoder {
    /**
     * 将url编码的unicode转成utf-8编码的字符串
     *
     * @param input 类似"%u7ecf%u5178%u7406%u8bba"的格式
     * @return 解码的字符串
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
