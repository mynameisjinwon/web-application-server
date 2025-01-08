# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답

- 요청 url 추출해 해당하는 파일을 클라이언트에 전달하는 코드 추가

```java
// HTTP header 받아오기
BufferedReader br = new BufferedReader(new InputStreamReader(in));
String url = br.readLine().split(" ")[1];
log.debug("url : {} ", url);

byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
```

- http 헤더의 첫번째 줄 두번째 요소가 url, url을 추출해 해당하는 파일을 ./webapp 폴더에서 찾아 클라이언트에 전달한다.

### 요구사항 2 - get 방식으로 회원가입
- url 과 함께 전달된 파라미터들을 파싱해 User 객체로 생성
```java
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
```
- url과 파라미터를 구분하는 '?'를 기준으로 문자열을 ulr, param 으로 나눈뒤 
- HttpRequestUtils 의 parseQueryString()을 사용해 파싱한다. 
- 그리고 새로운 User 객체를 생성한다.

### 요구사항 3 - post 방식으로 회원가입
* Post 방식으로 요청이 오면 데이터는 url 이 아닌 HTTP Body 로 전달이 된다.
* HTTP body 로 전달된 데이터는 HTTP header 이후 한줄 다음부터 시작된다.
* HTTP body 데이터를 읽기위해 Content-Length 값을 알아야한다.
* Content-Length 값을 적시에 추출하기 위해 headerMap 이라는 맵을 생성했다.
```java
Map<String, String> headerMap = new HashMap<>();
String line;
while (!"".equals(line=br.readLine())) {
    if(line == null) break;
    log.debug("line : {}", line);
    String[] tokens = line.split(":");
    headerMap.put(tokens[0], tokens[1].trim());
} 
```
* HTTP body 에 있는 회원 정보를 읽어 새로운 User 객체를 생성했다.
```java
// Post 방식으로 회원가입 요청이 오면
if ("POST".equals(method) && "/user/create".equals(url)) {
    String httpBody = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
    log.debug("HTTP body : {}", httpBody);

    Map<String, String> parMap = HttpRequestUtils.parseQueryString(httpBody);

    User user = new User(parMap.get("userId"), parMap.get("password"), parMap.get("name"), parMap.get("email"));
    log.debug("new user : {}", user);
}
```
### 요구사항 4 - redirect 방식으로 이동
* Redirect 를 위해선 응답 헤더의 status 코드를 302로 전달해야한다. 
* `HTTP/1.1 302 Redirect`
* Location 을 지정해 원하는 url 로 리다이렉트 시킨다. 
```java
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
```
```java
private void response302Header(DataOutputStream dos, String location) {
    try {
        dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
        dos.writeBytes("Location : " + location);
        dos.writeBytes("\r\n");
    } catch (IOException e) {
        log.error(e.getMessage());
    }
}
```
* 회원가입을 완료하면 * 응답 데이터로 전달할 것이 없으니 /index.html 로 리다이렉트 후 종료한다.

### 요구사항 5 - cookie
* 응답 헤더에 Set-Cookie 속성을 추가해 Cookie를 생성할 수 있다.
* Cookie 의 Path 를 설정하면 Cookie 의 적용 범위를 설정할 수 있다.
* /user/login.html. 페이지에서 쿠키를 설정하면 /user 의 하위 경로에서만 적용된다.
* /index.html 에서는 적용되지 않는 문제가 있었다.
* Path=/ 설정을 추가해 웹사이트의 모든 경로에서 쿠키가 적용되도록했다.
```java
if ("/user/login".equals(url)) {
    String loginId = parMap.get("userId");
    User userById = DataBase.findUserById(loginId);

    // 회원가입되지 않은 아이디인경우
    if (userById == null) {
    response302Header(dos, "/user/login_failed.html");
    log.debug("존재하지 않는 회원");
    return;
    }

    // 비밀번호가 일치하지 않는 경우
    if (!userById.getPassword().equals(parMap.get("password"))) {
    response302Header(dos, "/user/login_failed.html");
    log.debug("비밀번호 불일치 ");
    return;
    }

    //비밀번호가 일치하면 로그인 성공
    response302HeaderLoginSuccess(dos, "/index.html");
    log.debug("로그인 성공");
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
```
### 요구사항 6 - 사용자 목록 출력
* lgoined 쿠키 값이 true 일 경우 /user/list 에 접근하면 유저 목록을 출력한다.
* 그렇지 않으면 login.html 로 리다이렉트한다.
* logined 쿠키 값을 불리언으로 변환해 false 일 경우 login.html 로 리다이렉트, true 인 경우 유저 목록을 보여주도록 했다.
```java
Boolean isLogined = false;
if (headerMap.containsKey("Cookie")) {
    log.debug("Cookie : {} ", headerMap.get("Cookie"));
    Map<String, String> cookie = HttpRequestUtils.parseCookies(headerMap.get("Cookie"));
    log.debug("cookie : {} ", cookie);
    if (cookie.containsKey("logined")) {
        isLogined = Boolean.parseBoolean(cookie.get("logined"));
        log.debug("isLogined : {}", isLogined);
    }
}

// /user/list 에 접속했을 때
if ("/user/list".equals(url)) {
    // 로그인 안했으면 
    if (!isLogined) {
        response302Header(dos, "/user/login.html");
        return;
}
    // 로그인 했으면
    response302Header(dos, "/user/list.html");
}
```
### 요구사항 7 - stylesheet 적용
* css 파일을 적용시키기 위해선 http header 에 해당 파일이 css 파일이라는것을 명시해줘야한다.
* `Content-type : text/css` 이렇게 해서 할 수 있다.
```java
 // css 적용
if (url.contains("css")) {
    log.debug("css response");
    byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
    response200HeaderCss(dos, body.length );
    responseBody(dos, body);
    return;
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
```

### heroku 서버에 배포 후
* 