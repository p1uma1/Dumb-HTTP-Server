package org.example.DumbHttpServer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ByteStreamReader implements Closeable {
    private final InputStream in;
    private final Socket clientSocket;

    public ByteStreamReader(InputStream in, int size, Socket clientSocket) {
        this.in = in;
        this.clientSocket = clientSocket;
    }

    public String read() throws IOException {
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
        int b;
        int bodyIndex = -1;

        while ((b = in.read()) != -1) {
            headerBuf.write(b);
            String h = headerBuf.toString(StandardCharsets.UTF_8);
            if ((bodyIndex = h.indexOf("\r\n\r\n")) > 0) {
                bodyIndex += 3;
                break;
            }
            if ((bodyIndex = h.indexOf("\n\n")) > 0) {
                bodyIndex += 1;
                break;
            }
        }

        if (bodyIndex > 0) {
            byte[] headers = headerBuf.toByteArray();
            RequestParser req = new RequestParser(new String(headers, StandardCharsets.UTF_8), this.clientSocket);
            req.parseRequest();

            String contentLengthRaw = req.getHeaders(req.getHeaders()).get("Content-Length");
            int contentLength = (contentLengthRaw == null) ? 0 : Integer.parseInt(contentLengthRaw);

            for (int i = 0; i < contentLength; i++) {
                b = in.read();
                if (b == -1) {
                    break;
                }
                headerBuf.write(b);
            }
            return new String(headerBuf.toByteArray(), StandardCharsets.UTF_8);
        }
        return "Error";
    }

    @Override
    public void close() throws IOException {
        // Socket stream lifecycle is managed by server code.
    }
}
