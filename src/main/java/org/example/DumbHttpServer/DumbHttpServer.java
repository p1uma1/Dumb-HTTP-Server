package org.example.DumbHttpServer;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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


        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter( clientSocket.getOutputStream())
        );
        String inputLine;
        String requestString = "";
        while((inputLine=in.readLine())!=null && !inputLine.isEmpty()){
            requestString+=inputLine+'\n';
            if(inputLine.isEmpty()) break;
        }

        System.out.print("request body: \n"+new RequestHandler(requestString,clientSocket).passBody());

        System.out.print("raw request \n"+requestString);
        // DumbHttpResponse = new RequestHandler(requestString,clientSocket).getHttpResponse();

        LocalDateTime now = LocalDateTime.now();

        out.write("HTTP/1.1 200 OK\r\nDate: " + now + "\r\nServer: Custom Server\r\nContent-Type: text/html\r\nContent-Length: " + length + "\r\n\r\n");
        out.write(body);
        out.flush();


    }
}