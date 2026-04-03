package org.example.DumbHttpServer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ByteStreamReader implements Closeable {
    private final InputStream in;
    private final Socket clientSocket;

    public ByteStreamReader(InputStream in, int size, Socket clientSocket) {
        this.in = in;
        this.clientSocket = clientSocket;
    }

    public String read() throws IOException {
        ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int bytesRead;
        int headerEnd = -1;

        while ((bytesRead = in.read(chunk)) != -1) {
            requestBuffer.write(chunk, 0, bytesRead);
            byte[] current = requestBuffer.toByteArray();
            headerEnd = findHeaderEnd(current);
            if (headerEnd != -1) {
                break;
            }
        }

        if (headerEnd == -1) {
            return "Error";
        }

        byte[] initial = requestBuffer.toByteArray();
        String headersText = new String(initial, 0, headerEnd, StandardCharsets.UTF_8);
        int contentLength = extractContentLength(headersText);

        int bodyAlreadyRead = initial.length - headerEnd;
        int remainingBodyBytes = Math.max(0, contentLength - bodyAlreadyRead);

        while (remainingBodyBytes > 0 && (bytesRead = in.read(chunk, 0, Math.min(chunk.length, remainingBodyBytes))) != -1) {
            requestBuffer.write(chunk, 0, bytesRead);
            remainingBodyBytes -= bytesRead;
        }

        return new String(requestBuffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i + 4;
            }
        }
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] == '\n' && data[i + 1] == '\n') {
                return i + 2;
            }
        }
        return -1;
    }

    private int extractContentLength(String headersText) {
        String[] lines = headersText.split("\\r?\\n");
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
            if (!"content-length".equals(key)) {
                continue;
            }
            String value = line.substring(idx + 1).trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        // Socket stream lifecycle is managed by server code.
    }
}
