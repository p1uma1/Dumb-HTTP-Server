package org.example;

import org.example.DumbHttpServer.DumbHttpServer;
import org.example.DumbHttpServer.RequestHandler;
import org.example.contesxt.HttpContext;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        int port = 8080;
        DumbHttpServer dummyServer = new DumbHttpServer(port);
        HttpContext app = dummyServer.getHttpContext();
        RequestHandler bookController = request -> "<h1>endpoint function here</h1>";
        app.get("/api",bookController);
        dummyServer.listen();
    }
}
