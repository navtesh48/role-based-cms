package cms.repo;

import cms.model.Article;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryArticleRepository implements ArticleRepository {
    private final Map<Long, Article> articles = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    @Override public long nextId() { return ids.incrementAndGet(); }
    @Override public void save(Article article) { articles.put(article.id(), article); }
    @Override public Optional<Article> findById(long id) { return Optional.ofNullable(articles.get(id)); }
    @Override public boolean delete(long id) { return articles.remove(id) != null; }

    @Override
    public List<Article> findAll() {
        List<Article> all = new ArrayList<>(articles.values());
        all.sort(Comparator.comparing(Article::updatedAt).reversed());
        return all;
    }
}
