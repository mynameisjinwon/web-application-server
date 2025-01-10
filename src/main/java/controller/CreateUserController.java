package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    // 회원가입 기능 구현
    void doPost(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        User dbUser = DataBase.findUserById(request.getParameter("userId"));
        if (dbUser != null) {
            response.sendRedirect("/user/form.html");
            log.error("이미 존재하는 회원 아이디");
            return;
        }

        if( userId != null && password != null && name != null && email != null) {
            User user = new User(userId, password, name, email);
            DataBase.addUser(user);
            log.debug("회원가입 성공 : {} ", userId);
            response.sendRedirect("/index.html");
            return;
        }

        response.sendRedirect("/user/form.html");
        log.error("필수값 입력 안함");
    }

    @Override
    // 회원가입 화면을 보여준다.
    void doGet(HttpRequest request, HttpResponse response) {
        response.forward("/user/form.html");
    }
}
