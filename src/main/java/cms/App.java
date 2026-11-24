package cms;

import cms.controller.ArticleController;
import cms.controller.AuthController;
import cms.model.Role;
import cms.repo.InMemoryArticleRepository;
import cms.repo.InMemoryUserRepository;
import cms.security.SessionManager;
import cms.service.ArticleService;
import cms.service.AuthService;
import cms.view.LoginView;
import java.time.Clock;
import java.time.Duration;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Composition root: wires repositories, services, controllers and the first view. */
public final class App {
    private App() {}

    public static void main(String[] args) {
        Clock clock = Clock.systemUTC();
        AuthService auth = new AuthService(new InMemoryUserRepository(), new SessionManager(clock, Duration.ofMinutes(30)));
        ArticleService articleService = new ArticleService(new InMemoryArticleRepository(), auth, clock);

        // Demo accounts so the app is usable on first run.
        auth.register("admin", "Admin1234", Role.ADMIN);
        auth.register("alice", "Alice1234", Role.USER);

        AuthController authController = new AuthController(auth);
        ArticleController articleController = new ArticleController(articleService, authController);

        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            new LoginView(authController, articleController).setVisible(true);
        });
    }
}
