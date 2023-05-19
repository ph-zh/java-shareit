package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.controller.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto save(long userId, ItemDto itemDto) {
        userService.getById(userId);
        Item item = itemRepository.save(userId, itemMapper.toItem(itemDto));
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllUserItems(long userId) {
        userService.getById(userId);
        return itemMapper.toListOfItemDto(itemRepository.findAllUserItems(userId));
    }

    @Override
    public @Valid ItemDto update(long itemId, long userId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item with id: %d not found", itemId)));

        if (item.getOwner() != userId) {
            throw new NotFoundException(String.format("item with id: %d " +
                    "does not belong to the user with id: %d", itemId, userId));
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(long itemId, long userId) {
        userService.getById(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item with id: %d not found", itemId)));

        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        userService.getById(userId);

        return itemMapper.toListOfItemDto(itemRepository.search(text));
    }
}
