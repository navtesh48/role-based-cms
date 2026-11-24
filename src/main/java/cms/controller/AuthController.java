package cms.controller;

import cms.model.Role;
import cms.model.User;
import cms.service.AuthService;
import java.util.List;

/** Holds the current session for the GUI and delegates to {@link AuthService}. */
public final class AuthController {
    private final AuthService auth;
    private String token;

    public AuthController(AuthService auth) { this.auth = auth; }

    public User login(String username, String password) {
        token = auth.login(username, password);
        return auth.requireUser(token);
    }

    public void signUp(String username, String password) { auth.signUp(username, password); }

    public void logout() {
        auth.logout(token);
        token = null;
    }

    public String token() { return token; }
    public User current() { return auth.requireUser(token); }
    public List<User> users() { return auth.listUsers(token); }
    public void setRole(long userId, Role role) { auth.setRole(token, userId, role); }
}
