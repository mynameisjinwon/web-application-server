package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private final DataOutputStream dos;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream outputStream) {
        dos = new DataOutputStream(outputStream);
    }

    // HTTP Response header 만 보냄
    public void forward(String path) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
            String contentType = "text/html";

            if (path.endsWith("css")) {
                contentType = "text/css";
            }

            if (path.endsWith("js")) {
                contentType = "application/javascript";
            }

            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if(!headers.isEmpty()) {
                sendHeader();
            }
            dos.writeBytes("Content-Type: " + contentType +";charset=utf-8 \r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");

            dos.write(body, 0, body.length);
            dos.flush();

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    // HTTP response body 를 보냄
//    public void forwardBody(String path) {
//        try {
//            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
//            dos.write(body, 0, body.length);
//            dos.flush();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//
//    }

    public void addHeader(String fieldName, String value) {
        headers.put(fieldName, value);
    }

    public void sendRedirect(String Location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect\r\n");
            if(!headers.isEmpty()) {
                sendHeader();
            }
            dos.writeBytes("Location: " + Location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");

            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void sendHeader() throws IOException {
        for (String key : headers.keySet()) {
            dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
        }
    }
}
