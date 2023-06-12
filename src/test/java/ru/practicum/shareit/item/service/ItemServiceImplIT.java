package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIT {

    private final ItemService itemService;
    private final ItemRepository itemRepository;

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private Comment comment;
    private Booking booking1;
    private Booking booking2;
    private ItemDtoWithBookingsAndComments itemDtoWithBookingsAndComments1;
    private ItemDtoWithBookingsAndComments itemDtoWithBookingsAndComments2;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm");

    @BeforeEach
    void addContext() {
        Instant instantNow = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter)
                .toInstant(ZoneOffset.UTC);
        LocalDateTime localDateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter);

        user1 = userRepository.save(User.builder()
                .name("name1")
                .email("email1@email.ru")
                .build());

        user2 = userRepository.save(User.builder()
                .name("name2")
                .email("email2@email.ru")
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
                .build());

        comment = commentRepository.save(Comment.builder()
                .text("text")
                .item(item1)
                .author(user2)
                .created(instantNow)
                .build());

        booking1 = bookingRepository.save(Booking.builder()
                .start(localDateTime.minusDays(10))
                .end(localDateTime.minusDays(1))
                .item(item2)
                .booker(user2)
                .status(Status.APPROVED)
                .build());

        booking2 = bookingRepository.save(Booking.builder()
                .start(localDateTime.plusDays(1))
                .end(localDateTime.plusDays(10))
                .item(item2)
                .booker(user2)
                .status(Status.APPROVED)
                .build());

        itemDtoWithBookingsAndComments1 = ItemDtoWithBookingsAndComments.builder()
                .id(item1.getId())
                .name("item1")
                .description("desc1")
                .available(true)
                .comments(List.of(commentMapper.toCommentDto(comment)))
                .build();

        itemDtoWithBookingsAndComments2 = ItemDtoWithBookingsAndComments.builder()
                .id(item2.getId())
                .name("item2")
                .description("desc2")
                .available(true)
                .lastBooking(bookingMapper.toBookingDtoForItem(booking1))
                .nextBooking(bookingMapper.toBookingDtoForItem(booking2))
                .comments(Collections.emptyList())
                .build();
    }

    @Test
    void getAllUserItems_whenFromAndSizeAreMissing_thenReturnedAllItemsOfUser() {
        assertEquals(itemService.getAllUserItems(user1.getId(), 0, 10),
                List.of(itemDtoWithBookingsAndComments1, itemDtoWithBookingsAndComments2));
    }

    @Test
    void getAllUserItems_whenFromAndSizeAreValid_thenReturnedContentOfPage() {
        assertEquals(itemService.getAllUserItems(user1.getId(), 0, 1),
                List.of(itemDtoWithBookingsAndComments1));
    }

    @Test
    void getById_whenParamsAreValid_thenReturnedOwnerItemWithBookings() {
        assertEquals(itemService.getById(item2.getId(), user1.getId()), itemDtoWithBookingsAndComments2);
    }

    @Test
    void getById_whenParamsAreValid_thenReturnedItemWithoutBookingsForNotOwner() {
        itemDtoWithBookingsAndComments2.setNextBooking(null);
        itemDtoWithBookingsAndComments2.setLastBooking(null);

        assertEquals(itemService.getById(item2.getId(), user2.getId()), itemDtoWithBookingsAndComments2);
    }

    @Test
    void getById_whenItemIsNotExist_thenReturnedNotFoundExceptionThrown() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getById(1000L, user1.getId()));

        assertEquals(exception.getMessage(), "item with id: 1000 not found");
    }

    @Test
    void addComment_whenAllParamsAreValid_thenSavedAndReturnedExceptedObjectFromDb() {
        long userId = user2.getId();
        long itemId = item2.getId();
        CommentCreationDto commentCreationDto = new CommentCreationDto();
        commentCreationDto.setText("comment");

        CommentDto actualCommentDto = itemService.addComment(userId, itemId, commentCreationDto);

        CommentDto savedCommentDto = commentMapper.toCommentDto(commentRepository.findById(actualCommentDto.getId())
                .orElseThrow(() -> new NotFoundException("comment does not saved in db")));

        Instant actual = LocalDateTime.parse(LocalDateTime.ofInstant(actualCommentDto.getInstant(),
                ZoneOffset.UTC).format(formatter), formatter).toInstant(ZoneOffset.UTC);
        actualCommentDto.setInstant(actual);

        Instant saved = LocalDateTime.parse(LocalDateTime.ofInstant(savedCommentDto.getInstant(),
                ZoneOffset.UTC).format(formatter), formatter).toInstant(ZoneOffset.UTC);
        savedCommentDto.setInstant(saved);

        assertEquals(actualCommentDto, savedCommentDto);
    }

    @Test
    void addComment_whenUserHasNeverBeenBookedInThePast_thenReturnedBadRequestException() {
        long userId = user1.getId();
        long itemId = item3.getId();
        CommentCreationDto commentCreationDto = new CommentCreationDto();

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.addComment(userId, itemId, commentCreationDto));

        assertEquals(exception.getMessage(), String.format("Booker with id: %d has never booked items in the past",
                userId));
    }

    @Test
    void addComment_whenItemHasNeverBeenBookedFromUser_thenReturnedBadRequestException() {
        long userId = user2.getId();
        long itemId = item1.getId();
        CommentCreationDto commentCreationDto = new CommentCreationDto();

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                itemService.addComment(userId, itemId, commentCreationDto));

        assertEquals(exception.getMessage(), String.format("The booker with id: %d has not yet booked an item with id: %d" +
                " or the booked period has not yet expired", userId, itemId));
    }

    @AfterEach
    void deleteContext() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
    }
}