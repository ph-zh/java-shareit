package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto add(long userId, ItemRequestCreationDto itemRequestCreationDto);

    List<ItemRequestDto> getAllByOwner(long userId);

    List<ItemRequestDto> getAll(long userId, Integer from, Integer size);

    ItemRequestDto getById(long userId, long requestId);
}
