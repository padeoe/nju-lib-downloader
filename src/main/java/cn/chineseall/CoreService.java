package cn.chineseall;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by padeoe on 2017/4/10.
 */
public class CoreService {
    private String username;
    private String password;
    public static final String baseUrl = "http://sxqh.chineseall.cn";
    public CoreService(String username, String password){
        this.username=username;
        this.password=password;
    }
    public  String getSession() throws IOException {
        Map<String, String> attr = new HashMap<>();
        attr.put("Referer", baseUrl+"/sso/login.jsps?redirectUrl="+baseUrl);
        attr.put("Origin", baseUrl);
        String result = getCookie("userName=" + username + "&userPass=" + password + "&redirectUrl="+ URLEncoder.encode(baseUrl), baseUrl + "/sso/logon.jsps", attr, "UTF-8", 3000);
        return result;
    }

    private  String getCookie(String data, String URL, Map<String, String> requestProperty,String inputEncoding, int timeout) throws IOException {
        byte[] dataAsBytes = new byte[]{};
        if (data != null) {
            dataAsBytes = data.getBytes(inputEncoding);
        }
        java.net.URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080)));
        connection.setConnectTimeout(timeout);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        if (requestProperty != null) {
            for (Map.Entry<String, String> entry : requestProperty.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (data != null) {
            connection.setRequestProperty("Content-Length", String.valueOf(dataAsBytes.length));
        }
        connection.setInstanceFollowRedirects(false);
        connection.connect();
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
        utils.network.MyByteArray myByteArray = new utils.network.MyByteArray();
        Map<String, List<String>> headers = connection.getHeaderFields();


        connection.disconnect();
        byte[] bytes = new byte[myByteArray.getSize()];
        System.arraycopy(myByteArray.getBuffer(), 0, bytes, 0, bytes.length);
        return headers.get("Set-Cookie").get(0);
    }
}
