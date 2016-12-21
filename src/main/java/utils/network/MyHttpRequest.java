package utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 该类用于负责http网络请求，包含get，set等方法
 *
 * @author padeoe, Nifury
 *         Date: 2016/12/09
 */
public class MyHttpRequest {
    public static String[] action(String action, String data, String URL, Map<String, String> requestProperty, String cookie, String inputEncoding, String outputEncoding, int timeout) throws IOException {
        ReturnData returnData = action_returnbyte(action, data, URL, requestProperty, cookie, inputEncoding, timeout);
        String result = null;
        if (returnData.data != null) {
            result = new String(returnData.data, 0, returnData.data.length, outputEncoding);
        }
        List<String> cookies = returnData.getHeaders().get("Set-Cookie");
        if (cookies != null && cookies.get(0) != null) {
            return new String[]{result, cookies.get(0)};
        }
        return new String[]{result};
    }

    /**
     * POST请求
     *
     * @param action          post或get请求
     * @param data            数据
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param cookie          cookie若无则置为空
     * @param inputEncoding   请求编码
     * @param timeout         超时时间
     * @return 字符串数组，第一个元素是响应数据,若长度为2则第二个是返回的cookie
     * @throws IOException 网络错误
     */
    public static ReturnData action_returnbyte(String action, String data, String URL, Map<String, String> requestProperty, String cookie, String inputEncoding, int timeout) throws IOException {
        byte[] dataAsBytes = new byte[]{};
        if (data != null) {
            dataAsBytes = data.getBytes(inputEncoding);
        }
        java.net.URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        connection.setConnectTimeout(timeout);
        connection.setRequestMethod(action);
        if (action.toLowerCase().equals("post")) {
            connection.setDoOutput(true);
        }
        //  connection.setUseCaches(false);
           /*           java 1.6 does not support
           requestProperty.forEach((k,v) -> connection.setRequestProperty(k, v));
           */
        if (requestProperty != null) {
            for (Map.Entry<String, String> entry : requestProperty.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (data != null) {
            connection.setRequestProperty("Content-Length", String.valueOf(dataAsBytes.length));
        }

        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }
        connection.connect();

           /*          java 1.6 do not support
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(dataAsBytes);
            }*/
        if (data != null) {
            OutputStream outputStream = null;
            try {
                outputStream = connection.getOutputStream();
                outputStream.write(dataAsBytes);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }

            }
        }

        //读取返回数据
        utils.network.MyByteArray myByteArray = new utils.network.MyByteArray();
/*          java 1.6 do not support
            try (InputStream inputStream = connection.getInputStream()) {
                len = inputStream.read(readData);
            }*/
        InputStream inputStream = null;

        try {
            inputStream = connection.getInputStream();
            while (true) {
                myByteArray.ensureCapacity(4096);
                int len = inputStream.read(myByteArray.getBuffer(), myByteArray.getOffset(), 4096);
                if (len == -1) {
                    break;
                }
                myByteArray.addOffset(len);
            }

        } finally {
            if (inputStream != null) {
                {
                    inputStream.close();
                }
            }
        }

        Map<String, List<String>> headers = connection.getHeaderFields();
        connection.disconnect();
        return new ReturnData(myByteArray.getBuffer(), headers);
    }

    /**
     * 获得cookie的POST请求
     *
     * @param postData        请求数据
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param inputEncoding   请求编码
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 字符串数组，第一个元素是响应数据,第二个是返回的cookie
     * @throws IOException 网络错误
     */
    public static String[] postAndGetCookie(String postData, String URL, Map<String, String> requestProperty, String inputEncoding, String outputEncoding, int timeout) throws IOException {
        return action("POST", postData, URL, requestProperty, null, inputEncoding, outputEncoding, timeout);
    }

    /**
     * 发送cookie的POST请求
     *
     * @param postData        请求数据
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param cookie          发送的cookie
     * @param inputEncoding   请求编码
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 响应数据
     * @throws IOException 网络错误
     */
    public static String postWithCookie(String postData, String URL, Map<String, String> requestProperty, String cookie, String inputEncoding, String outputEncoding, int timeout) throws IOException {
        return action("POST", postData, URL, requestProperty, cookie, inputEncoding, outputEncoding, timeout)[0];
    }

    /**
     * POST请求(不含cookie)
     *
     * @param postData        请求数据
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param inputEncoding   请求编码
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 响应数据
     * @throws IOException 网络错误
     */
    public static String post(String postData, String URL, Map<String, String> requestProperty, String inputEncoding, String outputEncoding, int timeout) throws IOException {
        return action("POST", postData, URL, requestProperty, null, inputEncoding, outputEncoding, timeout)[0];
    }


    /**
     * 获得cookie的Get请求
     *
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 字符串数组，第一个元素是响应数据,第二个是返回的cookie
     * @throws IOException 网络错误
     */
    public static String[] getAndGetCookie(String URL, Map<String, String> requestProperty, String outputEncoding, int timeout) throws IOException {
        return action("GET", null, URL, requestProperty, null, "null", outputEncoding, timeout);
    }

    /**
     * 需要cookie的Get请求
     *
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param cookie          发送的cookie
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 响应数据
     * @throws IOException 网络错误
     */
    public static String getWithCookie(String URL, Map<String, String> requestProperty, String cookie, String outputEncoding, int timeout) throws IOException {
        return action("GET", null, URL, requestProperty, cookie, null, outputEncoding, timeout)[0];
    }

    /**
     * POST请求(不含cookie)
     *
     * @param URL             服务器地址
     * @param requestProperty 请求头
     * @param outputEncoding  响应编码
     * @param timeout         超时时间
     * @return 响应数据
     * @throws IOException 网络错误
     */
    public static String get(String URL, Map<String, String> requestProperty, String outputEncoding, int timeout) throws IOException {
        return action("GET", null, URL, requestProperty, null, null, outputEncoding, timeout)[0];
    }

    public static int getReturnCode(String action, String postData, String URL, Map<String, String> requestProperty, String inputEncoding, String outputEncoding, int timeout) {
        try {
            byte[] postAsBytes = new byte[]{};
            if (postData != null) {
                postAsBytes = postData.getBytes(inputEncoding);
            }
            java.net.URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(timeout);
            connection.setDoOutput(true);
            connection.setRequestMethod(action);
            connection.setUseCaches(false);
           /*           java 1.6 does not support
           requestProperty.forEach((k,v) -> connection.setRequestProperty(k, v));
           */
            if (requestProperty != null) {
                for (Map.Entry<String, String> entry : requestProperty.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setRequestProperty("Content-Length", String.valueOf(postAsBytes.length));
            connection.connect();
            int code = connection.getResponseCode();
            connection.disconnect();
            return code;
        } catch (UnsupportedEncodingException e) {
            System.out.println(e);
            return -1;
        } catch (MalformedURLException malformedURLException) {
            System.out.println(malformedURLException);
            return -2;
        } catch (ProtocolException protocolException) {
            System.out.println(protocolException);
            return -3;
        } catch (IOException ioException) {
            System.out.println(ioException);
            return -4;

        }
    }
}
