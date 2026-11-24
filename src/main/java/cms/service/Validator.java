package cms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Server-side validation. Views may pre-check, but this is the source of truth. */
public final class Validator {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,20}");
    public static final int MAX_TITLE = 100;
    public static final int MAX_BODY = 5000;

    private Validator() {}

    public static void checkCredentials(String username, String password) {
        List<String> errors = new ArrayList<>();
        if (username == null || !USERNAME.matcher(username).matches()) {
            errors.add("Username must be 3-20 characters: letters, digits or underscore.");
        }
        if (password == null || password.length() < 8
                || password.chars().noneMatch(Character::isLetter)
                || password.chars().noneMatch(Character::isDigit)) {
            errors.add("Password must be at least 8 characters and contain a letter and a digit.");
        }
        throwIfAny(errors);
    }

    public static void checkArticle(String title, String body) {
        List<String> errors = new ArrayList<>();
        if (title == null || title.isBlank()) errors.add("Title is required.");
        else if (title.length() > MAX_TITLE) errors.add("Title must be at most " + MAX_TITLE + " characters.");
        if (body == null || body.isBlank()) errors.add("Body is required.");
        else if (body.length() > MAX_BODY) errors.add("Body must be at most " + MAX_BODY + " characters.");
        throwIfAny(errors);
    }

    private static void throwIfAny(List<String> errors) {
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}
