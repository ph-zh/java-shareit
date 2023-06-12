package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;

import java.util.List;

public interface ItemService {

    ItemDto save(long userId, ItemDto itemDto);

    List<ItemDtoWithBookingsAndComments> getAllUserItems(long userId);

    ItemDto update(long itemId, long userId, ItemDto itemDto);

    ItemDtoWithBookingsAndComments getById(long itemId, long userId);

    List<ItemDto> search(long userId, String text);

    CommentDto addComment(long userId, long itemId, CommentCreationDto commentCreationDto);
}
