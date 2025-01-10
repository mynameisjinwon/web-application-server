package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import controller.Controller;
import controller.CreateUserController;
import controller.LoginController;
import controller.UserListController;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private HttpRequest request;
    private HttpResponse response;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // HTTP Header 를 관리하는 별도의 클래스 HttpRequest
            request = new HttpRequest(in);
            // HTTP response 를 처리하는 별도의 클래스 HttpResponse
            response = new HttpResponse(out);

            String url = request.getPath();
            String method = request.getMethod();

//            Map<String, String> headerMap = new HashMap<>();
//            String line;
//            while (!"".equals(line=br.readLine())) {
//                if(line == null) break;
//                String[] tokens = line.split(":");
//                headerMap.put(tokens[0], tokens[1].trim());
//            }

            Map<String, Controller> controllerMap = new HashMap<>();
            controllerMap.put("/user/create", new CreateUserController());
            controllerMap.put("/user/login", new LoginController());
            controllerMap.put("/user/list", new UserListController());

            if ("/".equals(url)) {
                log.debug("url : {} redirect to /index.html!", url);
                response.sendRedirect("/index.html");
                return;
            }

            if (url.endsWith("logout")) {
                log.debug("로그아웃한다이다");
                response.addHeader("Set-Cookie", "logined=false;Path=/");
                response.sendRedirect("/index.html");
                return;
            }

            if (controllerMap.get(url) == null) {
                log.debug("no controllers were called, url : {}", url);
                response.forward(url);
                return;
            }

            log.debug("controller is called : {}", controllerMap.get(url));
            controllerMap.get(url).service(request, response);
/*
            Boolean isLogined = false;
            if (request.getHeader("Cookie") != null) {
                log.debug("Cookie : {} ", request.getHeader("Cookie"));
                Map<String, String> cookie = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
                if (cookie.containsKey("logined")) {
                    isLogined = Boolean.parseBoolean(cookie.get("logined"));
                    log.debug("isLogined : {}", isLogined);
                }
            }

            // url 이 없으면 index.html 로 리다이렉트
            if ("/".equals(request.getPath()) || "".equals(request.getPath()) || request.getPath() == null) {
                response.sendRedirect("/index.html");
                return;
            }

            // css 적용
//            if (url.endsWith("css")) {
//                log.debug("css response");
//                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                response200HeaderCss(dos, body.length );
//                responseBody(dos, body);
//                return;
//            }

            // 요청 url 에 파라미터가 포함되어 있으면 쿼리스트링 파싱
            if (url.contains("?")) {
                // User 객체 생성
                User user = new User(request.getParameter("userId"), request.getParameter("password"),
                        request.getParameter("name"), request.getParameter("email"));
                log.debug("new user : {}", user);
            }

            // Post 방식으로 요청이 오면
            if ("POST".equals(method)) {

                // 회원 가입
                if ("/user/create".equals(url)) {
                    User newUser = new User(request.getParameter("userId"), request.getParameter("password"),
                            request.getParameter("name"), request.getParameter("email"));
                    //db 에 저장
                    DataBase.addUser(newUser);
                    log.debug("회원가입 성공! new user : {}", newUser);

                    //index.html 로 리다이렉트
                    response.sendRedirect("/index.html");
                    return;
                }

                // 로그인
                if ("/user/login".equals(url)) {
                    String loginId = request.getParameter("userId");
                    if (loginId == null) {
                        response.sendRedirect("/user/login_failed.html");
                        log.debug("아이디 is null");
                        return;
                    }
                    User userById = DataBase.findUserById(loginId);

                    // 회원가입되지 않은 아이디인경우
                    if (userById == null) {
                        response.sendRedirect("/user/login_failed.html");
                        log.debug("존재하지 않는 회원");
                        return;
                    }

                    // 비밀번호가 일치하지 않는 경우
                    if (!userById.getPassword().equals(request.getParameter("password"))) {
                        response.sendRedirect("/user/login_failed.html");
                        log.debug("비밀번호 불일치 ");
                        return;
                    }

                    //비밀번호가 일치하면 로그인 성공
                    response.addHeader("Set-Cookie", "logined=true; Path=/");
                    response.sendRedirect("/index.html");
                    log.debug("로그인 성공");
                }
            }
            // 로그아웃
            if ("/user/logout".equals(url)) {
                response.addHeader("Set-Cookie", "logined=false; Path=/");
                response.sendRedirect("/index.html");
            }

            // /user/list 에 접속했을 때
            if ("/user/list".equals(url)) {
                if (!isLogined) {
                    response.sendRedirect("/user/login.html");
                    return;
                }

                response.responseBody(getUserListTable());
            }

            response.forward(request.getPath());
 */

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] getUserListTable() {
        StringBuffer sb = new StringBuffer();
        Collection<User> users = DataBase.findAll();
        sb.append("<table border=1>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString().getBytes();
    }

    private void response200Header(DataOutputStream dos, int LengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + LengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location : " + location);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderLoginSuccess(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie : logined=true; Path=/\r\n");
            dos.writeBytes("Location : " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderLogout(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie : logined=false; Path=/\r\n");
            dos.writeBytes("Location : " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
