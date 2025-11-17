package org.example.DumbHttpServer;

import org.example.httpServer.utils.ByteStreamReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DumbHttpServer {
    private ServerSocket serverSocket;
    private final int port;
    private static final int THREAD_POOL_SIZE = 10;


    public DumbHttpServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = null;
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
        String body = """
                <html>
                    <head>
                        <title>Home</title>
                    </head>
                    <body>
                        <h1>Home Page</h1>
                        <p>Java Tutorials</p>
                        <ul>
                            <li>
                                <a href="/get-started-with-java-series"> Java </a>
                            </li>
                            <li>
                                <a href="/spring-boot"> Spring </a>
                            </li>
                            <li>
                                <a href="/learn-jpa-hibernate"> Hibernate </a>
                            </li>
                        </ul>
                     </body>
                 </html>
            """;
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        int length = bodyBytes.length;

        InputStream input = clientSocket.getInputStream();
        ByteArrayInputStream b;

        
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter( clientSocket.getOutputStream())
        );

        ByteStreamReader in = new ByteStreamReader(input,1024,clientSocket);
        String headers = in.read();
        System.out.println(headers);




            LocalDateTime now = LocalDateTime.now();

            out.write("HTTP/1.1 200 OK\r\nDate: " + now + "\r\nServer: Custom Server\r\nContent-Type: text/html\r\nContent-Length: " + length + "\r\n\r\n");
            out.write(body);
            out.flush();
        }




    }
