package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryIT {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final ItemRequestRepository itemRequestRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private ItemRequest itemRequest;

    @BeforeEach
    void addContext() {
        user1 = userRepository.save(User.builder()
                .name("name1")
                .email("email1@email.ru")
                .build());

        user2 = userRepository.save(User.builder()
                .name("name2")
                .email("email2@email.ru")
                .build());

        itemRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("itemRequest")
                .requestor(user1.getId())
                .build());

        item1 = itemRepository.save(Item.builder()
                .name("item1")
                .description("desc1")
                .available(true)
                .owner(user1.getId())
                .build());

        item2 = itemRepository.save(Item.builder()
                .name("item2")
                .description("desc2")
                .available(true)
                .owner(user1.getId())
                .build());

        item3 = itemRepository.save(Item.builder()
                .name("bicycle")
                .description("good bicycle")
                .available(true)
                .owner(user2.getId())
                .build());

        item4 = itemRepository.save(Item.builder()
                .name("BICYCLE")
                .description("OLD BICYCLE")
                .available(false)
                .owner(user2.getId())
                .request(itemRequest.getId())
                .build());
    }

    @AfterEach
    void deleteContext() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByOwner_whenOwnerExist_thenReturnListOfOwnerItems() {
        List<Item> result = itemRepository.findAllByOwner(user1.getId(), Pageable.unpaged()).getContent();

        assertEquals(result, List.of(item1, item2));
    }

    @Test
    void findAllByOwner_whenOwnerNotFound_thenReturnEmptyList() {
        List<Item> result = itemRepository.findAllByOwner(1000L, Pageable.unpaged()).getContent();

        assertEquals(result, Collections.emptyList());
    }

    @Test
    void search_whenItemContainSearchText_thenReturnListOfItemsWithTextInNameOrDescription() {
        List<Item> result = itemRepository.search("bicycle", Pageable.unpaged()).getContent();

        assertEquals(result, List.of(item3));
    }

    @Test
    void findAllByRequest() {
        List<Item> result = itemRepository.findAllByRequestIn(List.of(itemRequest.getId()));

        assertEquals(result, List.of(item4));
    }
}