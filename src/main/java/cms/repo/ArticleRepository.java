package cms.repo;

import cms.model.Article;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    long nextId();
    void save(Article article);
    Optional<Article> findById(long id);
    List<Article> findAll();
    boolean delete(long id);
}
