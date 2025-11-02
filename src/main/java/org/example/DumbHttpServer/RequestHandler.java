package org.example.DumbHttpServer;

import java.net.Socket;
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
    public HttpRequest parseRequest(){
        String[] request= this.rawString.split("\r?\n\r?\n");
        String body ="";
        if(request.length==2){
            body= request[1];
        }
        //seperate first line from request upper body
        String[] lines = request[0].split("\r?\n", 2);

        if (lines.length == 0 || lines[0].trim().isEmpty()) {
            throw new RuntimeException("BAD REQUEST") ;
        }
        String method = lines[0];  //method line
        String headersString = lines[1];
        String httpMethod="";
        String httpVersion="";
        String path="/";

        String[] parts = method.split(" ");
        if (parts.length < 1) {
            throw new RuntimeException("BAD REQUEST");
        }

        else {
            httpMethod=parts[0].trim().toUpperCase();
            if (parts.length == 3) {
                //ex : GET/POST
                path = parts[1].trim();
                httpVersion = parts[2].trim();
            }
        }
        Map<String,String> headers = getHeaders(headersString);
        return new HttpRequest(method,path,httpVersion,headers,body);
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