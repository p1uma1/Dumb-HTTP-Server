package org.example.DumbHttpServer;

import java.net.Socket;
import java.util.HashMap;

public class RequestParser {
    private final String rawString;
    private String headers;
    private final Socket clientSocket;

    public RequestParser(String rawString, Socket clientSocket) {
        this.rawString = rawString;
        this.clientSocket = clientSocket;
    }

    public HttpRequest parseRequest() {
        if (this.rawString == null || this.rawString.isEmpty()) {
            throw new RuntimeException("BAD REQUEST");
        }

        String[] headAndBody = this.rawString.split("\r?\n\r?\n", 2);
        String head = headAndBody[0];
        this.headers = head;
        String body = (headAndBody.length == 2) ? headAndBody[1] : "";

        String[] headLines = head.split("\r?\n", 2);
        if (headLines.length == 0 || headLines[0].trim().isEmpty()) {
            throw new RuntimeException("BAD REQUEST");
        }

        String startLine = headLines[0].trim();
        String headersString = (headLines.length == 2) ? headLines[1] : "";

        String[] parts = startLine.split("\\s+");
        if (parts.length < 3) {
            throw new RuntimeException("BAD REQUEST");
        }

        String httpMethod = parts[0].trim().toUpperCase();
        String requestTarget = parts[1].trim();
        String httpVersion = parts[2].trim();
        String path = requestTarget.isEmpty() ? "/" : requestTarget;

        HashMap<String, String> parsedHeaders = getHeaders(headersString);
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
            int idx = line.indexOf(':');
            if (idx > 0 && idx < line.length() - 1) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }
}
