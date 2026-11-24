package cms.repo;

import cms.model.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    @Override public long nextId() { return ids.incrementAndGet(); }
    @Override public void save(User user) { users.put(user.id(), user); }
    @Override public Optional<User> findById(long id) { return Optional.ofNullable(users.get(id)); }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public List<User> findAll() {
        List<User> all = new ArrayList<>(users.values());
        all.sort(Comparator.comparingLong(User::id));
        return all;
    }
}
