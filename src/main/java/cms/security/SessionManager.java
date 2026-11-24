package cms.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side sessions identified by an unguessable token, with sliding expiry. */
public final class SessionManager {
    private record Session(long userId, Instant expiresAt) {}

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;
    private final Duration ttl;

    public SessionManager(Clock clock, Duration ttl) {
        this.clock = clock;
        this.ttl = ttl;
    }

    public String create(long userId) {
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        sessions.put(token, new Session(userId, clock.instant().plus(ttl)));
        return token;
    }

    /** Returns the user id for a live token and extends the session; empty if unknown or expired. */
    public Optional<Long> resolve(String token) {
        if (token == null) return Optional.empty();
        Session s = sessions.get(token);
        if (s == null) return Optional.empty();
        if (!clock.instant().isBefore(s.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }
        sessions.put(token, new Session(s.userId(), clock.instant().plus(ttl)));
        return Optional.of(s.userId());
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }
}
