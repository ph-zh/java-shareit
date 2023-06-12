package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;


    @Test
    void save_whenUserExist_thenSaveItem() {
        long userId = 1L;
        ItemDto itemDtoToSave = new ItemDto();

        when(itemMapper.toItem(any(ItemDto.class))).thenReturn(new Item());
        Item item = itemMapper.toItem(itemDtoToSave);
        item.setOwner(userId);

        when(itemRepository.save(item)).thenReturn(item);

        ItemDto expectedItemDto = ItemDto.builder().id(userId).build();
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(expectedItemDto);

        ItemDto actualItemDto = itemService.save(userId, itemDtoToSave);

        assertEquals(expectedItemDto, actualItemDto);
    }

    @Test
    void save_whenUserNotFound_thenNotFoundExceptionThrown() {
        ItemDto itemDto = ItemDto.builder().build();
        when(userService.getById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> itemService.save(0L, itemDto));

        Item item = Item.builder().build();
        verify(itemRepository, never()).save(item);
    }

    @Test
    void update_whenParamsAreValid_thenReturnedUpdatedItem() {
        long userId = 1L;
        long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("newItem1")
                .description("newDesc")
                .available(false)
                .build();

        Item item = Item.builder()
                .id(itemId)
                .name("item1")
                .description("desc")
                .available(true)
                .owner(userId)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemDto expectedItemDto = itemMapper.toItemDto(item);
        ItemDto actualItemDto = itemService.update(itemId, userId, itemDto);

        assertEquals(actualItemDto, expectedItemDto);
    }

    @Test
    void update_whenUserIdIsNotOwnerId_thenReturnedNotFoundException() {
        long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = new ItemDto();

        Item item = Item.builder()
                .id(itemId)
                .owner(1L)
                .build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, userId, itemDto));

        assertEquals(exception.getMessage(), "item with id: 1 does not belong to the user with id: 2");
    }

    @Test
    void search_whenFromAndSizeAreValid_thenReturnedPageableListOfItems() {
        long userId = 1L;
        String text = "text";
        int from = 0;
        int size = 3;
        List<Item> items = List.of(new Item());
        Page<Item> itemsPage = new PageImpl<>(items);

        when(itemRepository.search(text, PageRequest.of(from / size, size))).thenReturn(itemsPage);

        List<ItemDto> expectedList = itemMapper.toListOfItemDto(itemsPage.getContent());
        List<ItemDto> actualList = itemService.search(userId, text, from, size);

        assertEquals(expectedList, actualList);
    }
}