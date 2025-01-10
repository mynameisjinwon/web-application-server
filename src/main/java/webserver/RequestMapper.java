package webserver;

import controller.*;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    private static Map<String, Controller> controllers = new HashMap<>();

    static {
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new UserListController());
        controllers.put("/user/logout", new LogoutController());
    }

    public static Controller getController(String url) {
        return controllers.get(url);
    }

}
