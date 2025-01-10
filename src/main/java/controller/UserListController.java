package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;

public class UserListController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(UserListController.class);
    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        log.error("이게 호출되면 안됩니다.");
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if (isLogined(request)) {
            log.debug("logined = true");
            response.responseBody(getUserListTable());
            return;
        }
        response.sendRedirect("/user/login.html");
    }

    private boolean isLogined(HttpRequest request) {
        try {
            return Boolean.parseBoolean(request.getCookieValue("logined"));
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private byte[] getUserListTable() {
        try {
            StringBuffer sb = new StringBuffer();
            Collection<User> users = DataBase.findAll();
            sb.append("<table border=1>");
            for (User user : users) {
                sb.append("<tr>");
//                sb.append("<td>" + user.getUserId() + "</td>");
//                sb.append("<td>" + user.getName() + "</td>");
//                sb.append("<td>" + user.getEmail() + "</td>");
                sb.append("<td>" + URLDecoder.decode(user.getUserId(), "utf-8") + "</td>");
                sb.append("<td>" + URLDecoder.decode(user.getName(), "utf-8") + "</td>");
                sb.append("<td>" + URLDecoder.decode(user.getEmail(), "utf-8") + "</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");

            return sb.toString().getBytes();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
