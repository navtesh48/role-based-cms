package cms.repo;

import cms.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    long nextId();
    void save(User user);
    Optional<User> findById(long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
}
