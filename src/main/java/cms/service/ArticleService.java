package cms.service;

import cms.model.Article;
import cms.model.Permission;
import cms.model.User;
import cms.repo.ArticleRepository;
import java.time.Clock;
import java.util.List;

/** CRUD for articles with role-based access control enforced on every call. */
public final class ArticleService {
    private final ArticleRepository articles;
    private final AuthService auth;
    private final Clock clock;

    public ArticleService(ArticleRepository articles, AuthService auth, Clock clock) {
        this.articles = articles;
        this.auth = auth;
        this.clock = clock;
    }

    public List<Article> list(String token) {
        auth.requireUser(token);
        return articles.findAll();
    }

    public Article create(String token, String title, String body) {
        User u = auth.require(token, Permission.CREATE_ARTICLE);
        Validator.checkArticle(title, body);
        Article a = new Article(articles.nextId(), title.trim(), body.trim(), u.id(), u.username(),
                clock.instant(), clock.instant());
        articles.save(a);
        return a;
    }

    public Article update(String token, long id, String title, String body) {
        User u = auth.requireUser(token);
        Article existing = find(id);
        boolean allowed = u.role().can(Permission.EDIT_ANY)
                || (u.role().can(Permission.EDIT_OWN) && existing.authorId() == u.id());
        if (!allowed) throw new AccessDeniedException("You can only edit your own articles.");
        Validator.checkArticle(title, body);
        Article updated = existing.withContent(title.trim(), body.trim(), clock.instant());
        articles.save(updated);
        return updated;
    }

    public void delete(String token, long id) {
        User u = auth.requireUser(token);
        Article existing = find(id);
        boolean allowed = u.role().can(Permission.DELETE_ANY)
                || (u.role().can(Permission.DELETE_OWN) && existing.authorId() == u.id());
        if (!allowed) throw new AccessDeniedException("You can only delete your own articles.");
        articles.delete(id);
    }

    private Article find(long id) {
        return articles.findById(id).orElseThrow(() -> new ValidationException(List.of("Article not found.")));
    }
}
