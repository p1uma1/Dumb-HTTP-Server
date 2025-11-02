package org.example.DumbHttpServer;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler {
    private String rawString;
    private Socket clientSocket;


    public RequestHandler(String rawString, Socket clientSocket) {
        this.rawString = rawString;
        this.clientSocket = clientSocket;

    }

//    public HttpRequest passRequest(){
//        String[] headerBody=this.rawString.split("\r\n\n");
//        String Header = headerBody[0];
//        String body=headerBody[1];
//
//    }
    /*
    * POST /users HTTP/1.1
        Host: example.com
        Content-Type: application/x-www-form-urlencoded
        Content-Length: 49

        name=FirstName+LastName&email=bsmth%40example.com
    * */


    public HttpRequest parseRequest() {
        if (this.rawString == null || this.rawString.isEmpty()) {
            throw new RuntimeException("BAD REQUEST");
        }

        // 1) Separate head and body (limit = 2)
        String[] headAndBody = this.rawString.split("\r?\n\r?\n", 2);
        String head = headAndBody[0];
        String body = (headAndBody.length == 2) ? headAndBody[1] : "";

        // 2) Extract the start-line (first line) and the raw headers string
        String[] headLines = head.split("\r?\n", 2);
        if (headLines.length == 0 || headLines[0].trim().isEmpty()) {
            throw new RuntimeException("BAD REQUEST");
        }
        String startLine = headLines[0].trim();
        String headersString = (headLines.length == 2) ? headLines[1] : "";

        // 3) Parse the start-line
        String[] parts = startLine.split("\\s+");  //seperate strings between multiple whitespace characters
        if (parts.length < 3) {
            throw new RuntimeException("BAD REQUEST");
        }
        String httpMethod = parts[0].trim().toUpperCase();
        String requestTarget = parts[1].trim();
        String httpVersion = parts[2].trim();
        String path;

        path = requestTarget.isEmpty() ? "/" : requestTarget;


        Map<String, String> headers = getHeaders(headersString);

        // (Optional) Basic Content-Length sanity check : do this in later version
//        String cl = headers.get("Content-Length");
//        if (cl != null) {
//            try {
//                int expected = Integer.parseInt(cl.trim());
//                if (expected < 0) throw new NumberFormatException();
//                // NOTE: body.length() is characters, not bytes; proper check needs raw bytes/encoding
//                if (body.getBytes(StandardCharsets.UTF_8).length < expected) {
//                    throw new RuntimeException("BAD REQUEST: Body shorter than Content-Length");
//                }
//            } catch (NumberFormatException ignore) {
//                // Ignore bad Content-Length; many servers would reject this
//            }
//        }

        // 6) Build your object — pass the METHOD, not the whole start line
        return new HttpRequest(httpMethod, path, httpVersion, headers, body);
    }


    public String getPath() {
        if (this.rawString == null || this.rawString.isEmpty()) {
            return "/";
        }
        String[] lines = this.rawString.split("\r?\n", 2);
        if (lines.length == 0) return "/";
        String[] parts = lines[0].split(" ");
        if (parts.length < 2) return "/";
        return parts[1].trim();
    }


    public Map<String,String> getHeaders(String headers){

        Map map = new HashMap();

        if (headers == null || headers.isEmpty()) {
            return map;
        }

        String[] lines = headers.split("\r?\n"); // handle both \r\n and \n

        for (String line : lines ){
            int idx = line.indexOf(':');
            if (idx > 0 && idx < line.length() - 1) {
                String key = line.substring(0, idx).trim();  //remove spaces
                String value = line.substring(idx + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }

//    public HttpRequest getHttpResponse() {
//
//        return  new HttpRequest();
//    }
}