package cms.service;

import cms.model.Permission;
import cms.model.Role;
import cms.model.User;
import cms.repo.UserRepository;
import cms.security.PasswordHasher;
import cms.security.SessionManager;
import java.util.List;

/** Registration, login, sessions and user administration. */
public final class AuthService {
    private final UserRepository users;
    private final SessionManager sessions;

    public AuthService(UserRepository users, SessionManager sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public User register(String username, String password, Role role) {
        Validator.checkCredentials(username, password);
        if (users.findByUsername(username).isPresent()) {
            throw new ValidationException(List.of("That username is already taken."));
        }
        String salt = PasswordHasher.newSalt();
        User user = new User(users.nextId(), username, salt, PasswordHasher.hash(password, salt), role);
        users.save(user);
        return user;
    }

    /** Public sign-up always creates a regular USER. */
    public User signUp(String username, String password) {
        return register(username, password, Role.USER);
    }

    /** Returns a session token. The error message never reveals whether the username exists. */
    public String login(String username, String password) {
        User user = users.findByUsername(username == null ? "" : username).orElse(null);
        if (user == null) {
            PasswordHasher.hash(password == null ? "" : password, PasswordHasher.newSalt()); // equalize timing
            throw new AuthException("Invalid username or password.");
        }
        if (!PasswordHasher.verify(password == null ? "" : password, user.salt(), user.passwordHash())) {
            throw new AuthException("Invalid username or password.");
        }
        return sessions.create(user.id());
    }

    public void logout(String token) { sessions.invalidate(token); }

    public User requireUser(String token) {
        long id = sessions.resolve(token).orElseThrow(() -> new AuthException("Please sign in again."));
        return users.findById(id).orElseThrow(() -> new AuthException("Account no longer exists."));
    }

    public User require(String token, Permission permission) {
        User u = requireUser(token);
        if (!u.role().can(permission)) {
            throw new AccessDeniedException("Your role (" + u.role() + ") cannot do this.");
        }
        return u;
    }

    public List<User> listUsers(String token) {
        require(token, Permission.MANAGE_USERS);
        return users.findAll();
    }

    public void setRole(String token, long userId, Role newRole) {
        User actor = require(token, Permission.MANAGE_USERS);
        User target = users.findById(userId).orElseThrow(() -> new ValidationException(List.of("User not found.")));
        boolean demotingLastAdmin = target.role() == Role.ADMIN && newRole != Role.ADMIN
                && users.findAll().stream().filter(u -> u.role() == Role.ADMIN).count() == 1;
        if (demotingLastAdmin) {
            throw new ValidationException(List.of("Cannot demote the last remaining admin."));
        }
        users.save(target.withRole(newRole));
        if (actor.id() == target.id() && newRole != Role.ADMIN) {
            sessions.invalidate(token); // lost privileges: force re-login
        }
    }
}
