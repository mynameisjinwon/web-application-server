package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private InputStream in;
    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, String> paramMap = new HashMap<>();
    private String method;
    private String path;
    private String httpVersion;

    public HttpRequest(InputStream in) throws IOException {
        this.in = in;
        init();
    }

    private void init() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String[] tokens = br.readLine().split(" ");

        if (tokens.length != 3) {
            return;
        }

        method = tokens[0];
        path = tokens[1];
        if (path.contains("?")) {
            int index = path.indexOf("?");
            String queryString = path.substring(index + 1);
            path = path.substring(0, index);

            setParameters(queryString);
        }
        httpVersion = tokens[2];

        setHeaders(br);

        if (headerMap.containsKey("Content-Length")) {
            setParameters(getBodyContent(br));
        }
    }

    private String getBodyContent(BufferedReader br) throws IOException {
        int length = Integer.parseInt(headerMap.get("Content-Length"));
        char[] body = new char[length];
        br.read(body, 0, length);

        return String.valueOf(body);
    }

    private void setHeaders(BufferedReader br) throws IOException {
        String line;
        while (!"".equals(line = br.readLine())) {
            if (line == null)  break;

            // 나눠진 배열의 길이를 2로 제한 Host: localHost:8080 같은 경우를 대비하기위해
            String[] splitStr = line.split(":", 2);
            headerMap.put(splitStr[0], splitStr[1].trim());
        }
    }

    private void setParameters(String queryString) {
        String[] qr = queryString.split("&");
        for (String s : qr) {
            String[] split = s.split("=");
            if (split.length < 2) continue;
            paramMap.put(split[0], split[1]);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getHeader(String filedName) {
        return headerMap.getOrDefault(filedName, null);
    }

    public String getParameter(String filedName) {
        return paramMap.getOrDefault(filedName, null);
    }




}
