package controller;

import controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class LogoutController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(LogoutController.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        log.error("이게 호출되면 안됩니다.");
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.addHeader("Set-Cookie", "logined=false; Path=/");
        response.sendRedirect("/index.html");
    }
}
