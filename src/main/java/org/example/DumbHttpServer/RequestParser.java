package org.example.DumbHttpServer;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RequestParser {
    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    private final String rawString;
    private String headers;
    private final Socket clientSocket;

    public RequestParser(String rawString, Socket clientSocket) {
        this.rawString = rawString;
        this.clientSocket = clientSocket;
    }

    public HttpRequest parseRequest() {
        if (this.rawString == null || this.rawString.isBlank()) {
            throw new BadRequestException("Empty request");
        }

        String[] headAndBody = this.rawString.split("\r?\n\r?\n", 2);
        String head = headAndBody[0];
        this.headers = head;
        String body = (headAndBody.length == 2) ? headAndBody[1] : "";

        String[] headLines = head.split("\r?\n", 2);
        if (headLines.length == 0 || headLines[0].trim().isEmpty()) {
            throw new BadRequestException("Missing request line");
        }

        String startLine = headLines[0].trim();
        String headersString = (headLines.length == 2) ? headLines[1] : "";

        String[] parts = startLine.split("\\s+");
        if (parts.length != 3) {
            throw new BadRequestException("Malformed request line");
        }

        String httpMethod = parts[0].trim().toUpperCase();
        String requestTarget = parts[1].trim();
        String httpVersion = parts[2].trim();
        if (!SUPPORTED_METHODS.contains(httpMethod)) {
            throw new BadRequestException("Unsupported method");
        }
        if (requestTarget.isEmpty() || !requestTarget.startsWith("/")) {
            throw new BadRequestException("Invalid request target");
        }
        if (!httpVersion.matches("HTTP/\\d\\.\\d")) {
            throw new BadRequestException("Invalid HTTP version");
        }
        String path = requestTarget;

        HashMap<String, String> parsedHeaders = getHeaders(headersString);
        validateHeaders(parsedHeaders, httpVersion, body);
        return new HttpRequest(httpMethod, path, httpVersion, parsedHeaders, body);
    }

    public String getPath() {
        if (this.rawString == null || this.rawString.isEmpty()) {
            return "/";
        }
        String[] lines = this.rawString.split("\r?\n", 2);
        if (lines.length == 0) {
            return "/";
        }
        String[] parts = lines[0].split(" ");
        if (parts.length < 2) {
            return "/";
        }
        return parts[1].trim();
    }

    public String getHeaders() {
        return this.headers;
    }

    public HashMap<String, String> getHeaders(String headersString) {
        HashMap<String, String> map = new HashMap<>();

        if (headersString == null || headersString.isEmpty()) {
            return map;
        }

        String[] lines = headersString.split("\r?\n");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            int idx = line.indexOf(':');
            if (idx <= 0 || idx == line.length() - 1) {
                throw new BadRequestException("Malformed header line");
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if (key.isEmpty()) {
                throw new BadRequestException("Empty header name");
            }
            map.put(key, value);
        }
        return map;
    }

    private void validateHeaders(Map<String, String> headersMap, String httpVersion, String body) {
        if ("HTTP/1.1".equals(httpVersion) && getHeaderIgnoreCase(headersMap, "Host") == null) {
            throw new BadRequestException("Missing Host header");
        }

        String contentLengthValue = getHeaderIgnoreCase(headersMap, "Content-Length");
        if (contentLengthValue == null) {
            return;
        }

        int expectedLength;
        try {
            expectedLength = Integer.parseInt(contentLengthValue.trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid Content-Length");
        }

        if (expectedLength < 0) {
            throw new BadRequestException("Invalid Content-Length");
        }

        int actualLength = body.getBytes(StandardCharsets.UTF_8).length;
        if (actualLength < expectedLength) {
            throw new BadRequestException("Body shorter than Content-Length");
        }
    }

    private String getHeaderIgnoreCase(Map<String, String> headersMap, String headerName) {
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).equals(headerName.toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        return null;
    }
}
