package webserver;

import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private InputStream in;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private HttpMethod method;
    private String path;
    private String httpVersion;

    public HttpRequest(InputStream in) throws IOException {
        this.in = in;
        init();
    }

    private void init() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        // 요청 라인 읽고, method, url version 저장
        String[] tokens = br.readLine().split(" ");

        if (tokens.length != 3) {
            return;
        }

        method = HttpMethod.valueOf(tokens[0]);
        path = tokens[1];
        // url 과 쿼리스트링이 같이 전달되면 쿼리스트링 분리
        if (path.contains("?")) {
            int index = path.indexOf("?");
            String queryString = path.substring(index + 1);
            path = path.substring(0, index);

            setParameters(queryString);
        }
        httpVersion = tokens[2];

        // 요청 헤더 읽어서 map 으로 저장
        setHeaders(br);

        // 요청본문으로 데이터가 전달되면
        if (headers.containsKey("Content-Length")) {
            setParameters(getBodyContent(br));
        }
    }

    // 본문 데이터 Content-Length 만큼 읽어서 리턴
    private String getBodyContent(BufferedReader br) throws IOException {
        int length = Integer.parseInt(headers.get("Content-Length"));
        char[] body = new char[length];
        br.read(body, 0, length);

        return String.valueOf(body);
    }

    // 요청 헤더 읽어서 map 으로 저장
    private void setHeaders(BufferedReader br) throws IOException {
        String line;
        while (!"".equals(line = br.readLine())) {
            if (line == null)  break;

            // 나눠진 배열의 길이를 2로 제한 Host: localHost:8080 같은 경우를 대비하기위해
            String[] splitStr = line.split(":", 2);
            headers.put(splitStr[0].trim(), splitStr[1].trim());
        }
    }

    private void setParameters(String queryString) {
        String[] qr = queryString.split("&");
        for (String s : qr) {
            String[] split = s.split("=");
            if (split.length < 2) continue;
            params.put(split[0], split[1]);
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getHeader(String filedName) {
        return headers.getOrDefault(filedName, null);
    }

    public String getParameter(String filedName) {
        return params.getOrDefault(filedName, null);
    }

    public String getCookieValue(String name) {
        if (!headers.containsKey("Cookie")) {
            return null;
        }
        Map<String, String> cookie = HttpRequestUtils.parseCookies(headers.get("Cookie"));
        if (!cookie.containsKey(name)) {
            return null;
        }
        return cookie.get(name);
    }



}
