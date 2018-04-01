package nz.timo.websocket.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HTTPRequestHeader extends HTTPHeader {
    private HTTPMethod method;
    private String path;

    public HTTPRequestHeader(String path, String host, int port, HTTPMethod method) {
        this.path = path;
        this.method = method;
        if (port != 80) {
            setHeader("Host", host + ":" + port);
        } else {
            setHeader("Host", host);
        }
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public ByteBuffer encode() {
        StringBuilder sb = new StringBuilder();
        // writing status line
        sb.append(method).append(" ").append(path).append(" ").append(Constants.HTTP_VERSION_STRING)
                .append(Constants.HTTP_NEW_LINE);

        for(Map.Entry<String, String> e : getHeaderFields().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(Constants.HTTP_NEW_LINE);
        }

        sb.append(Constants.HTTP_NEW_LINE);
        return StandardCharsets.US_ASCII.encode(sb.toString());
    }
}
