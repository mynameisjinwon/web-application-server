package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class LoginController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Override
        // 로그인 기능 구현
    public void doPost(HttpRequest request, HttpResponse response) {
        String loginUserId = request.getParameter("userId");
        String loginUserPw = request.getParameter("password");

        // 아이디 입력 x
        if (loginUserId == null) {
            response.sendRedirect("/user/login_failed.html");
            log.debug("아이디 = null");
            return;
        }

        // 비번 입력 x
        if (loginUserPw == null) {
            response.sendRedirect("/user/login_failed.html");
            log.debug("비번 = null");
            return;
        }

        User dbUser = DataBase.findUserById(loginUserId);
        // 존재하지 않는 회원
        if (dbUser == null) {
            response.sendRedirect("/user/login_failed.html");
            log.debug("존재하지 않는 회원");
            return;
        }

        // 로그인 성공
        if (dbUser.getPassword().equals(loginUserPw)) {
            log.debug("로그인 성공!");
            response.addHeader("Set-Cookie", "logined=true; Path=/");
            response.sendRedirect("/index.html");
            return;
        }

        response.sendRedirect("/user/login_failed.html");
        log.debug("비번 틀림 혹은 예상치 못한 오류");
    }

    @Override
        // 로그인 화면을 보여준다.
    public void doGet(HttpRequest request, HttpResponse response) {
        response.sendRedirect("/user/login.html");
    }
}
