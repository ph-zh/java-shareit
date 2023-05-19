package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(long id);

    List<User> findAll();

    User update(long id, User user);

    void delete(long id);
}
