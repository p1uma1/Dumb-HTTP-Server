package org.example.DumbHttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DumbHttpServerIntegrationTest {
    private static DumbHttpServer server;
    private static Thread serverThread;
    private static int port;

    @BeforeAll
    static void startServer() throws Exception {
        server = new DumbHttpServer(0);
        server.getHttpContext().get("/api", req -> "ok");

        serverThread = new Thread(() -> {
            try {
                server.listen();
            } catch (IOException ignored) {
                // Expected when stopping the server during test teardown.
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            port = server.getListeningPort();
            if (port > 0) {
                return;
            }
            Thread.sleep(25);
        }
        throw new IllegalStateException("Server did not start within timeout");
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void getApiReturns200() throws Exception {
        String response = sendRaw("GET /api HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        String response = sendRaw("GET /unknown HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 404 Not Found"));
    }

    @Test
    void wrongMethodReturns405() throws Exception {
        String response = sendRaw("POST /api HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 405 Method Not Allowed"));
    }

    @Test
    void malformedRequestReturns400() throws Exception {
        String response = sendRaw("BADREQUEST\r\n\r\n");
        assertTrue(response.startsWith("HTTP/1.1 400 Bad Request"));
    }

    private static String sendRaw(String request) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port);
             Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer.write(request);
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }
}
