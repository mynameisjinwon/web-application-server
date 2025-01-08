package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // HTTP header 받아오기
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String httpHeader = br.readLine();
            log.debug("HTTP Header : {}", httpHeader);

            String url = httpHeader.split(" ")[1];
            log.debug("url : {} ", url);

            // 요청 url 에 파라미터가 포함되어 있으면
            if (url.contains("?")) {
                int index = url.indexOf("?");
                String params = url.substring(index + 1);
                url = url.substring(0, index);
                log.debug("url : {}", url);
                log.debug("params : {}", params);

                // 파라미터 파싱
                HttpRequestUtils utils = new HttpRequestUtils();
                Map<String, String> parMap = utils.parseQueryString(params);

                // User 객체 생성
                User user = new User(parMap.get("userId"), parMap.get("password"), parMap.get("name"), parMap.get("email"));
                log.debug("new user : {}", user);
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
