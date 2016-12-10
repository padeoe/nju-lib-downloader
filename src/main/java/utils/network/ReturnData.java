package utils.network;

/**
 * Created by padeoe on 2016/5/12.
 */
public class ReturnData {
    byte[] data;
    String cookie;

    public ReturnData(byte[] data, String cookie) {
        this.cookie = cookie;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public String getCookie() {
        return cookie;
    }
}