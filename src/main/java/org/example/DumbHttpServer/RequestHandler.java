package org.example.DumbHttpServer;

@FunctionalInterface
public interface RequestHandler {
    String handle(HttpRequest request);
}
