package cms.model;

import java.time.Instant;

/** Immutable article; updates produce a new instance. */
public final class Article {
    private final long id;
    private final String title;
    private final String body;
    private final long authorId;
    private final String authorName;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Article(long id, String title, String body, long authorId, String authorName,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() { return id; }
    public String title() { return title; }
    public String body() { return body; }
    public long authorId() { return authorId; }
    public String authorName() { return authorName; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public Article withContent(String newTitle, String newBody, Instant now) {
        return new Article(id, newTitle, newBody, authorId, authorName, createdAt, now);
    }
}
