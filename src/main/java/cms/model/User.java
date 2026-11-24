package cms.model;

/** Immutable user record. Only a salted PBKDF2 hash of the password is stored. */
public final class User {
    private final long id;
    private final String username;
    private final String salt;
    private final String passwordHash;
    private final Role role;

    public User(long id, String username, String salt, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long id() { return id; }
    public String username() { return username; }
    public String salt() { return salt; }
    public String passwordHash() { return passwordHash; }
    public Role role() { return role; }

    public User withRole(Role newRole) {
        return new User(id, username, salt, passwordHash, newRole);
    }
}
