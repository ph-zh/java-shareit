package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {

    private int lastId = 0;
    private Map<Long, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(long id, User user) {
        user.setId(id);

        users.put(id, user);

        return user;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    private long getId() {
        return ++lastId;
    }
}
