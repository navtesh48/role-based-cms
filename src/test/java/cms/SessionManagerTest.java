package cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cms.security.SessionManager;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SessionManagerTest {
    /** Clock whose time the test controls. */
    static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void sessionExpiresAfterTtlAndSlidesWhileActive() {
        MutableClock clock = new MutableClock();
        SessionManager sm = new SessionManager(clock, Duration.ofMinutes(30));
        String token = sm.create(7);

        clock.now = clock.now.plus(Duration.ofMinutes(20));
        assertEquals(7L, sm.resolve(token).get());          // still valid, and slides forward
        clock.now = clock.now.plus(Duration.ofMinutes(20));
        assertTrue(sm.resolve(token).isPresent());          // 40 min total but only 20 idle
        clock.now = clock.now.plus(Duration.ofMinutes(31));
        assertTrue(sm.resolve(token).isEmpty());            // idle past ttl
    }

    @Test
    void tokensAreUnique() {
        SessionManager sm = new SessionManager(Clock.systemUTC(), Duration.ofMinutes(1));
        assertTrue(!sm.create(1).equals(sm.create(1)));
    }
}
