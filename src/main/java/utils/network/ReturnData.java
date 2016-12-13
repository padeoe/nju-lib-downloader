package utils.network;

import java.util.List;
import java.util.Map;

/**
 * Created by padeoe on 2016/5/12.
 */
public class ReturnData {
    byte[] data;
    Map<String, List<String>> headers;

    public ReturnData(byte[] data, Map<String, List<String>> headers) {
        this.data = data;
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}