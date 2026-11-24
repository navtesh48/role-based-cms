package cms.controller;

import cms.model.Article;
import cms.service.ArticleService;
import java.util.List;

public final class ArticleController {
    private final ArticleService service;
    private final AuthController session;

    public ArticleController(ArticleService service, AuthController session) {
        this.service = service;
        this.session = session;
    }

    public List<Article> list() { return service.list(session.token()); }
    public Article create(String title, String body) { return service.create(session.token(), title, body); }
    public Article update(long id, String title, String body) { return service.update(session.token(), id, title, body); }
    public void delete(long id) { service.delete(session.token(), id); }
}
