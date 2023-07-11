package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserService userService;

    @Test
    void add() {
        long userId = 1L;
        ItemRequestCreationDto itemRequestCreationDto = new ItemRequestCreationDto();

        ItemRequest itemRequest = ItemRequest.builder().build();
        when(itemRequestMapper.toItemRequest(itemRequestCreationDto)).thenReturn(itemRequest);

        ItemRequestDto expectedItemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .created(itemRequest.getCreated())
                .requestor(itemRequest.getRequestor())
                .build();

        when(itemRequestMapper.toItemRequestDto(itemRequest)).thenReturn(expectedItemRequestDto);

        ItemRequestDto actualItemRequestDto = itemRequestService.add(userId, itemRequestCreationDto);

        assertEquals(actualItemRequestDto, expectedItemRequestDto);

        verify(itemRequestRepository).save(itemRequest);
    }

    @Test
    void getAllByOwner() {
        long userId = 1L;

        List<ItemRequestDto> itemsRequestDto = List.of(ItemRequestDto.builder().id(1L).build());
        List<ItemRequest> itemsRequest = List.of(ItemRequest.builder().id(1L).build());

        when(itemRequestRepository.findAllByRequestor(userId, Sort.by(Sort.Direction.DESC, "created"))).thenReturn(itemsRequest);
        when(itemRequestMapper.toListOfItemRequestDto(itemsRequest)).thenReturn(itemsRequestDto);

        List<ItemDto> itemsDto = List.of();
        List<Item> items = List.of();

        when(itemRepository.findAllByRequestIn(anyList())).thenReturn(items);

        List<ItemRequestDto> actualList = itemRequestService.getAllByOwner(userId);

        assertEquals(actualList, itemsRequestDto);
    }

    @Test
    void getAll() {
        long userId = 1L;
        int from = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<ItemRequest> page = Page.empty();
        List<ItemRequestDto> list = List.of(ItemRequestDto.builder().id(1L).build());
        when(itemRequestRepository.findAllByRequestorNot(userId, pageable)).thenReturn(page);
        when(itemRequestMapper.toListOfItemRequestDto(page.getContent())).thenReturn(list);

        List<ItemDto> itemsDto = List.of();
        List<Item> items = List.of();

        when(itemRepository.findAllByRequestIn(anyList())).thenReturn(items);

        List<ItemRequestDto> actualList = itemRequestService.getAll(userId, from, size);

        assertEquals(actualList, list);
    }

    @Test
    void getById_whenItemRequestIsFound_thenReturnedCorrectObject() {
        long userId = 1L;
        long requestId = 1L;

        ItemRequest itemRequest = ItemRequest.builder().id(requestId).build();
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        List<ItemDto> itemsDto = List.of();
        List<Item> items = List.of();

        when(itemRepository.findAllByRequestIn(anyList())).thenReturn(items);
        when(itemMapper.toListOfItemDto(items)).thenReturn(itemsDto);

        ItemRequestDto expectedItemRequestDto = ItemRequestDto.builder().build();
        when(itemRequestMapper.toItemRequestDto(itemRequest)).thenReturn(expectedItemRequestDto);

        ItemRequestDto actualItemRequestDto = itemRequestService.getById(userId, requestId);

        assertEquals(expectedItemRequestDto, actualItemRequestDto);
    }

    @Test
    void getById_whenItemRequestIsNotFound_thenReturnedNotFoundException() {
        long userId = 1L;
        long requestId = 1L;

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.getById(userId, requestId));

        assertEquals(exception.getMessage(), String.format("item request with id: %d does not exist yet", requestId));
    }
}