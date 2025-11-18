package org.example.contesxt;

import org.example.middleware.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class HttpContext {
    private final Map<String, Map<String, RequestHandler>> routes;

    public HttpContext() {

        this.routes =new HashMap<>();
        this.routes.put("GET",new HashMap<>());
        this.routes.put("POST",new HashMap<>());
        this.routes.put("PUT",new HashMap<>());
        this.routes.put("DELETE",new HashMap<>());
    }

    public void get(String path, RequestHandler handler){
        routes.get("GET").put(path,handler);
    }

    public void post(String path,RequestHandler handler){
        routes.get("POST").put(path,handler);
    }
    public void put(String path,RequestHandler handler){
        routes.get("PUT").put(path,handler);
    }
    public void delete(String path,RequestHandler handler){
        routes.get("DELETE").put(path,handler);
    }

    public RequestHandler match(String method, String path) {
        return routes.getOrDefault(method, new HashMap<>()).get(path);
    }
}
