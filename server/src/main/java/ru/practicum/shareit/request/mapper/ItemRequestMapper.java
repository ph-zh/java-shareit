package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ItemRequestMapper {

    public ItemRequest toItemRequest(ItemRequestCreationDto itemRequestCreationDto) {
        return ItemRequest.builder()
                .description(itemRequestCreationDto.getDescription())
                .build();
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getRequestor())
                .created(itemRequest.getCreated())
                .build();
    }

    public List<ItemRequestDto> toListOfItemRequestDto(List<ItemRequest> list) {
        return list.stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }
}
