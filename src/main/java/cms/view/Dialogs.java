package cms.view;

import cms.service.AccessDeniedException;
import cms.service.AuthException;
import cms.service.ValidationException;
import java.awt.Component;
import javax.swing.JOptionPane;

/** Maps service exceptions to user-friendly dialogs. */
final class Dialogs {
    private Dialogs() {}

    static void error(Component parent, Exception e) {
        String title = e instanceof AccessDeniedException ? "Not allowed"
                : e instanceof AuthException ? "Sign-in problem"
                : e instanceof ValidationException ? "Please fix the following"
                : "Unexpected error";
        JOptionPane.showMessageDialog(parent, e.getMessage(), title, JOptionPane.WARNING_MESSAGE);
    }
}
