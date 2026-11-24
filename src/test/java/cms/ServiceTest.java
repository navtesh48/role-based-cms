package cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cms.model.Role;
import cms.repo.InMemoryArticleRepository;
import cms.repo.InMemoryUserRepository;
import cms.security.PasswordHasher;
import cms.security.SessionManager;
import cms.service.AccessDeniedException;
import cms.service.ArticleService;
import cms.service.AuthException;
import cms.service.AuthService;
import cms.service.ValidationException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceTest {
    AuthService auth;
    ArticleService articles;
    String admin, alice, bob;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        auth = new AuthService(new InMemoryUserRepository(), new SessionManager(clock, Duration.ofMinutes(30)));
        articles = new ArticleService(new InMemoryArticleRepository(), auth, clock);
        auth.register("admin", "Admin1234", Role.ADMIN);
        auth.signUp("alice", "Alice1234");
        auth.signUp("bob", "Bobby1234");
        admin = auth.login("admin", "Admin1234");
        alice = auth.login("alice", "Alice1234");
        bob = auth.login("bob", "Bobby1234");
    }

    @Test
    void passwordsAreHashedAndVerified() {
        String salt = PasswordHasher.newSalt();
        String h = PasswordHasher.hash("secret123", salt);
        assertFalse(h.contains("secret123"));
        assertTrue(PasswordHasher.verify("secret123", salt, h));
        assertFalse(PasswordHasher.verify("wrong", salt, h));
    }

    @Test
    void badLoginIsRejectedWithGenericMessage() {
        AuthException a = assertThrows(AuthException.class, () -> auth.login("alice", "nope"));
        AuthException b = assertThrows(AuthException.class, () -> auth.login("ghost", "nope"));
        assertEquals(a.getMessage(), b.getMessage());
    }

    @Test
    void weakCredentialsAndDuplicatesAreRejected() {
        assertThrows(ValidationException.class, () -> auth.signUp("a", "Valid1234"));
        assertThrows(ValidationException.class, () -> auth.signUp("validname", "short1"));
        assertThrows(ValidationException.class, () -> auth.signUp("ALICE", "Alice1234"));
    }

    @Test
    void logoutInvalidatesSession() {
        auth.logout(alice);
        assertThrows(AuthException.class, () -> articles.list(alice));
        assertThrows(AuthException.class, () -> articles.list("not-a-token"));
    }

    @Test
    void usersEditOnlyTheirOwnArticles() {
        long id = articles.create(alice, "Hello", "World").id();
        assertEquals("Edited", articles.update(alice, id, "Edited", "Body").title());
        assertThrows(AccessDeniedException.class, () -> articles.update(bob, id, "Hax", "Body"));
        assertThrows(AccessDeniedException.class, () -> articles.delete(bob, id));
    }

    @Test
    void adminCanEditAndDeleteAnything() {
        long id = articles.create(alice, "Hello", "World").id();
        articles.update(admin, id, "Moderated", "Body");
        articles.delete(admin, id);
        assertEquals(0, articles.list(admin).size());
    }

    @Test
    void articleValidation() {
        assertThrows(ValidationException.class, () -> articles.create(alice, " ", "body"));
        assertThrows(ValidationException.class, () -> articles.create(alice, "t", "x".repeat(5001)));
    }

    @Test
    void onlyAdminsManageUsers() {
        assertThrows(AccessDeniedException.class, () -> auth.listUsers(alice));
        assertEquals(3, auth.listUsers(admin).size());
        long aliceId = auth.listUsers(admin).stream().filter(u -> u.username().equals("alice")).findFirst().get().id();
        auth.setRole(admin, aliceId, Role.ADMIN);
        assertEquals(Role.ADMIN, auth.requireUser(alice).role());
    }

    @Test
    void lastAdminCannotBeDemoted() {
        long adminId = auth.requireUser(admin).id();
        assertThrows(ValidationException.class, () -> auth.setRole(admin, adminId, Role.USER));
    }
}
