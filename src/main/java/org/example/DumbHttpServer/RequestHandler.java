package org.example.DumbHttpServer;

@FunctionalInterface
public interface RequestHandler {
    void handle(HttpRequest request);
}
