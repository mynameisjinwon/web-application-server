package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

public abstract class AbstractController implements Controller {

    protected abstract void doPost(HttpRequest request, HttpResponse response);

    protected abstract void doGet(HttpRequest request, HttpResponse response);

    public void service(HttpRequest request, HttpResponse response) {
        if (request.getMethod().isPost()) {
            doPost(request, response);
            return;
        }
        doGet(request, response);
    }
}
