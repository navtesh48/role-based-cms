package cms.service;

/** The signed-in user's role does not allow this action. */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}
