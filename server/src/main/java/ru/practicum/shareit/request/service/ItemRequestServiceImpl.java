package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    @Override
    public @Valid ItemRequestDto add(long userId, ItemRequestCreationDto itemRequestCreationDto) {
        userService.getById(userId);

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestCreationDto);
        itemRequest.setRequestor(userId);
        itemRequest.setCreated(Instant.now());

        itemRequestRepository.save(itemRequest);

        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllByOwner(long userId) {
        userService.getById(userId);

        List<ItemRequestDto> result = itemRequestMapper.toListOfItemRequestDto(itemRequestRepository
                .findAllByRequestor(userId, Sort.by(Sort.Direction.DESC, "created")));

        return addItemsToRequests(result);
    }

    @Override
    public List<ItemRequestDto> getAll(long userId, Integer from, Integer size) {
        userService.getById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequestDto> result = itemRequestMapper.toListOfItemRequestDto(itemRequestRepository
                .findAllByRequestorNot(userId, pageable).getContent());

        return addItemsToRequests(result);
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        userService.getById(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("item request with id: %d does not exist yet",
                        requestId)));

        List<Long> requestsId = List.of(itemRequest.getId());

        List<ItemDto> items = itemMapper.toListOfItemDto(itemRepository.findAllByRequestIn(requestsId));

        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(items);

        return itemRequestDto;
    }

    private List<ItemRequestDto> addItemsToRequests(List<ItemRequestDto> result) {
        List<Long> requestsId = result.stream()
                .map(ItemRequestDto::getId)
                .collect(toList());

        Map<Long, List<ItemDto>> map = itemRepository
                .findAllByRequestIn(requestsId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId, toList()));

        for (ItemRequestDto itemRequestDto : result) {
            itemRequestDto.setItems(map.getOrDefault(itemRequestDto.getId(), Collections.emptyList()));
        }

        return result;
    }
}
