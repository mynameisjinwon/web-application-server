package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xpath.internal.objects.XNull;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
            DataOutputStream dos = new DataOutputStream(out);

            String httpHeader = br.readLine();
            log.debug("HTTP Header : {}", httpHeader);

            Map<String, String> headerMap = new HashMap<>();
            String line;
            while (!"".equals(line=br.readLine())) {
                if(line == null) break;
                String[] tokens = line.split(":");
                headerMap.put(tokens[0], tokens[1].trim());
            }

            String[] tokens = httpHeader.split(" ");
            String method = tokens[0];
            String url = tokens[1];

            // 요청 url 에 파라미터가 포함되어 있으면
            if (url.contains("?")) {
                int index = url.indexOf("?");
                String params = url.substring(index + 1);
                url = url.substring(0, index);
                log.debug("url : {}", url);
                log.debug("params : {}", params);

                // 파라미터 파싱
                Map<String, String> parMap = HttpRequestUtils.parseQueryString(params);

                // User 객체 생성
                User user = new User(parMap.get("userId"), parMap.get("password"), parMap.get("name"), parMap.get("email"));
                log.debug("new user : {}", user);
            }

            // Post 방식으로 회원가입 요청이 오면
            if ("POST".equals(method) && "/user/create".equals(url)) {
                String httpBody = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
                log.debug("HTTP body : {}", httpBody);

                Map<String, String> parMap = HttpRequestUtils.parseQueryString(httpBody);

                User user = new User(parMap.get("userId"), parMap.get("password"), parMap.get("name"), parMap.get("email"));
                log.debug("new user : {}", user);

                //index.html 로 리다이렉트
                response302Header(dos, "/index.html");
                return;

            }

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());


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
    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location : " + location);
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
