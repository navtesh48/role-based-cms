package cms.service;

/** Authentication failed or the session is missing/expired. */
public class AuthException extends RuntimeException {
    public AuthException(String message) { super(message); }
}
