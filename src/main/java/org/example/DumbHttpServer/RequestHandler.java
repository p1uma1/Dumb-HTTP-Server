package org.example.DumbHttpServer;

import java.net.Socket;

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
    public String passBody(){
        String[] request= this.rawString.split("\r\n\r\n");
        if(request.length==2){
            return request[1];
        }
        else return "";
    }
//    public String getMethod(){
//        this.rawString.
//    }

//    public HttpRequest getHttpResponse() {
//
//        return  new HttpRequest();
//    }
}