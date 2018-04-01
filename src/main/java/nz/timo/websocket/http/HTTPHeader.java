package nz.timo.websocket.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTTPHeader {
    private final Map<String, String> headerFields = new HashMap<>();

    public void setHeader(String headerName, String value) {
        headerFields.put(headerName, value);
    }

    public Map<String, String> getHeaderFields() {
        return Collections.unmodifiableMap(headerFields);
    }
}
