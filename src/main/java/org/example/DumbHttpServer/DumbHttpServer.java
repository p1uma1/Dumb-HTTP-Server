package org.example.DumbHttpServer;

import org.example.contesxt.HttpContext;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DumbHttpServer {
    private ServerSocket serverSocket;
    private final int port;
    private static final int THREAD_POOL_SIZE = 10;
    private final HttpContext httpContext;

    public HttpContext getHttpContext(){
        return this.httpContext;
    }


    public DumbHttpServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = null;
        this.httpContext=new HttpContext();
    }
    public void listen() throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.serverSocket=new ServerSocket(port);
        System.out.println("Server started on port "+port);
        while (true) {
            Socket clientSocket = this.serverSocket.accept();
            System.out.println("new client connect "+clientSocket.getInetAddress()+ " "+clientSocket.getPort());
            threadPool.execute(()-> {
                try {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

//    public void handleClient(Socket clientSocket) throws IOException {
//        String body = """
//                <html>
//                    <head>
//                        <title>Home</title>
//                    </head>
//                    <body>
//                        <h1>Home Page</h1>
//                        <p>Java Tutorials</p>
//                        <ul>
//                            <li>
//                                <a href="/get-started-with-java-series"> Java </a>
//                            </li>
//                            <li>
//                                <a href="/spring-boot"> Spring </a>
//                            </li>
//                            <li>
//                                <a href="/learn-jpa-hibernate"> Hibernate </a>
//                            </li>
//                        </ul>
//                     </body>
//                 </html>
//            """;
//        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
//        int length = bodyBytes.length;
//
//        InputStream input = clientSocket.getInputStream();
//
//
//        BufferedReader in =
//                new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8));
//        BufferedWriter out = new BufferedWriter(
//                new OutputStreamWriter( clientSocket.getOutputStream())
//        );
//        String inputLine = "";
//        StringBuilder requestHeaders = new StringBuilder();
//
//        while((inputLine=in.readLine())!=null && !inputLine.isEmpty()){
//            requestHeaders.append(inputLine).append("\r\n");
//        }
//        requestHeaders.append("\r\n");
//        while((inputLine=in.readLine())!=null && !inputLine.isEmpty()){
//            requestHeaders.append(inputLine).append("\r\n");
//        }
//        System.out.print(requestHeaders);
//        if(!requestHeaders.isEmpty())
//        {
//            RequestHandler requestHandler = new RequestHandler(requestHeaders.toString(), clientSocket);
//            HashMap<String, String> headers = requestHandler.getHeaders(requestHeaders.toString());
//            String reqbody = requestHandler.parseRequest().getBody();
//            String contentLength = headers.get("Content-Length");
//
//            System.out.print("body: \n" + reqbody);
//
//
////            int bufferSize = Integer.parseInt(contentLength);
////            byte[] buffer=new byte[bufferSize];
////            int totalBytesRead = 0;
////            int bytesRead=0;
//
////            while (totalBytesRead < bufferSize && (bytesRead = input.read(buffer, totalBytesRead, bufferSize - totalBytesRead)) != -1) {
////                totalBytesRead += bytesRead;
////                System.out.println("bytes read: "+bytesRead);
////            }
////            System.out.println(buffer);
//
//            LocalDateTime now = LocalDateTime.now();
//
//            out.write("HTTP/1.1 200 OK\r\nDate: " + now + "\r\nServer: Custom Server\r\nContent-Type: text/html\r\nContent-Length: " + length + "\r\n\r\n");
//            out.write(body);
//            out.flush();
//        }
//
//
//
//
//    }

    public void handleClient(Socket clientSocket) throws IOException {
        InputStream input = clientSocket.getInputStream();

        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter( clientSocket.getOutputStream())
        );

        ByteStreamReader in = new ByteStreamReader(input,1024,clientSocket);
        String statusLine = "HTTP/1.1 200 OK";
        String body = "";

        try {
            String request = in.read();
            HttpRequest request1 = new RequestParser(request,clientSocket).parseRequest();
            System.out.println("Method: "+request1.getMethod()+" path: "+request1.getPath());

            RequestHandler reqHandler = this.httpContext.match(request1.getMethod(),request1.getPath());
            if (reqHandler == null) {
                if (this.httpContext.hasPath(request1.getPath())) {
                    statusLine = "HTTP/1.1 405 Method Not Allowed";
                    body = "<h1>405 Method Not Allowed</h1>";
                } else {
                    statusLine = "HTTP/1.1 404 Not Found";
                    body = "<h1>404 Not Found</h1>";
                }
            } else {
                try {
                    body = reqHandler.handle(request1);
                    if (body == null) {
                        body = "";
                    }
                } catch (Exception ex) {
                    statusLine = "HTTP/1.1 500 Internal Server Error";
                    body = "<h1>500 Internal Server Error</h1>";
                }
            }
        } catch (BadRequestException ex) {
            statusLine = "HTTP/1.1 400 Bad Request";
            body = "<h1>400 Bad Request</h1>";
        } catch (Exception ex) {
            statusLine = "HTTP/1.1 500 Internal Server Error";
            body = "<h1>500 Internal Server Error</h1>";
        }

        out.write(HttpResponseBuilder.build(statusLine, "text/html; charset=UTF-8", body));
        out.flush();
    }




    }
