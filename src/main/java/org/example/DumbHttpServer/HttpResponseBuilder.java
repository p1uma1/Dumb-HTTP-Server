package org.example.DumbHttpServer;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class HttpResponseBuilder {
    private static final String SERVER_NAME = "DumbHttpServer";

    private HttpResponseBuilder() {
    }

    public static String build(String statusLine, String contentType, String body) {
        String safeBody = body == null ? "" : body;
        String safeContentType = contentType == null || contentType.isBlank()
                ? "text/plain; charset=UTF-8"
                : contentType;

        int contentLength = safeBody.getBytes(StandardCharsets.UTF_8).length;
        String dateHeader = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

        return statusLine + "\r\n"
                + "Date: " + dateHeader + "\r\n"
                + "Server: " + SERVER_NAME + "\r\n"
                + "Content-Type: " + safeContentType + "\r\n"
                + "Content-Length: " + contentLength + "\r\n"
                + "Connection: close\r\n"
                + "\r\n"
                + safeBody;
    }
}
