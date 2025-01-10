package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {

    abstract void doPost(HttpRequest request, HttpResponse response);

    abstract void doGet(HttpRequest request, HttpResponse response);

    public void service(HttpRequest request, HttpResponse response) {
        if ("POST".equals(request.getMethod())) {
            doPost(request, response);
            return;
        }
        doGet(request, response);
    }
}
