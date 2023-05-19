package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item save(long userId, Item item);

    List<Item> findAllUserItems(long userId);

    Item update(long itemId, long userId, Item item);

    Optional<Item> findById(long itemId);

    List<Item> search(String text);
}
