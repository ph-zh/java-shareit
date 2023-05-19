package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto save(long userId, ItemDto itemDto);

    List<ItemDto> getAllUserItems(long userId);

    ItemDto update(long itemId, long userId, ItemDto itemDto);

    ItemDto getById(long itemId, long userId);

    List<ItemDto> search(long userId, String text);
}
