package nz.timo.websocket.http;

import nz.timo.websocket.WebSocketConnectionException;

import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPHeaderDecoder {
    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile(Constants.HTTP_VERSION_STRING + " (\\d{3}) (.*)$");
    private static final int STATUS_CODE_GROUP = 1;

    private static final Pattern HEADER_FIELD_PATTERN = Pattern.compile("(.*?): (.*)");
    private static final int HEADER_FIELD_NAME_GROUP = 1;
    private static final int HEADER_FIELD_VALUE_GROUP = 2;

    private StringBuilder headerBuilder = new StringBuilder();

    public HTTPResponseHeader tryDecode(ByteBuffer data) {
        while(data.hasRemaining()) {
            char c = (char)data.get();
            if(c != '\r') {
                headerBuilder.append(c);
            }

            if(headerBuilder.length() >= 2 && headerBuilder.substring(headerBuilder.length() - 2)
                    .equals("\n\n")) {
                // done
                return constructHeader();
            }
        }

        return null;
    }

    private HTTPResponseHeader constructHeader() {
        Scanner scanner = new Scanner(headerBuilder.toString());

        String statusLine = scanner.nextLine();
        Matcher matcher = STATUS_LINE_PATTERN.matcher(statusLine);

        if(!matcher.find()) {
            throw new WebSocketConnectionException("Invalid status line received");
        }

        int statusCode = Integer.parseInt(matcher.group(STATUS_CODE_GROUP));
        HTTPResponseHeader header = new HTTPResponseHeader(statusCode);

        if(statusCode != 101) {
            throw new WebSocketConnectionException("Did not receive 101 status code, instead received " + statusCode);
        }

        // read the header fields
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if(line.isEmpty()) {
                // we're done
                break;
            }

            matcher = HEADER_FIELD_PATTERN.matcher(line);
            if(matcher.find()) {
                header.setHeader(matcher.group(HEADER_FIELD_NAME_GROUP), matcher.group(HEADER_FIELD_VALUE_GROUP));
            }
        }

        return header;
    }
}
