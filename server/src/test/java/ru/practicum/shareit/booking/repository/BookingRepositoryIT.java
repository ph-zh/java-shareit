package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryIT {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private Item item5;
    private Booking bookingInPastAndApprovedAndEndWas5DaysAgo;
    private Booking bookingInPastAndRejectedAndEndWas3DaysAgo;
    private Booking bookingInCurrentAndApprovedAndEndIn5Days;
    private Booking bookingInFutureAndApprovedAndEndIn7Days;
    private Booking bookingInFutureAndWaitingAndEndIn15Days;
    private Booking bookingFromBooker2InFutureAndStatusIsWaitingAndEndIn10Days;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm");
    private LocalDateTime localDateTimeNow = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter);
    private Pageable defaultPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "end"));

    @BeforeEach
    void addContext() {
        owner = userRepository.save(User.builder()
                .name("owner")
                .email("owner@mail.ru")
                .build());

        booker1 = userRepository.save(User.builder()
                .name("booker1")
                .email("booker1@mail.ru")
                .build());

        booker2 = userRepository.save(User.builder()
                .name("booker2")
                .email("booker2@mail.ru")
                .build());

        item1 = itemRepository.save(Item.builder()
                .name("item1")
                .description("desc")
                .available(true)
                .owner(owner.getId())
                .build());

        item2 = itemRepository.save(Item.builder()
                .name("item2")
                .description("desc")
                .available(true)
                .owner(owner.getId())
                .build());

        item3 = itemRepository.save(Item.builder()
                .name("item3")
                .description("desc")
                .available(true)
                .owner(owner.getId())
                .build());

        item4 = itemRepository.save(Item.builder()
                .name("item4")
                .description("desc")
                .available(true)
                .owner(owner.getId())
                .build());

        item5 = itemRepository.save(Item.builder()
                .name("item5")
                .description("desc")
                .available(true)
                .owner(owner.getId())
                .build());

        bookingInPastAndApprovedAndEndWas5DaysAgo = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.minusDays(10))
                .end(localDateTimeNow.minusDays(5))
                .booker(booker1)
                .item(item1)
                .status(Status.APPROVED)
                .build());

        bookingInPastAndRejectedAndEndWas3DaysAgo = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.minusDays(10))
                .end(localDateTimeNow.minusDays(3))
                .booker(booker1)
                .item(item2)
                .status(Status.REJECTED)
                .build());

        bookingInCurrentAndApprovedAndEndIn5Days = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.minusDays(2))
                .end(localDateTimeNow.plusDays(5))
                .booker(booker1)
                .item(item3)
                .status(Status.APPROVED)
                .build());

        bookingInFutureAndApprovedAndEndIn7Days = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.plusDays(2))
                .end(localDateTimeNow.plusDays(7))
                .booker(booker1)
                .item(item4)
                .status(Status.APPROVED)
                .build());

        bookingInFutureAndWaitingAndEndIn15Days = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.plusDays(10))
                .end(localDateTimeNow.plusDays(15))
                .booker(booker1)
                .item(item5)
                .status(Status.WAITING)
                .build());

        bookingFromBooker2InFutureAndStatusIsWaitingAndEndIn10Days = bookingRepository.save(Booking.builder()
                .start(localDateTimeNow.plusDays(1))
                .end(localDateTimeNow.plusDays(10))
                .booker(booker2)
                .item(item1)
                .status(Status.WAITING)
                .build());
    }

    @AfterEach
    void deleteContext() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByBookerId_whenPageableIsSet_thenReturnedListWithCorrectSizeAndRightSort() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "end"));
        List<Booking> result = bookingRepository.findAllByBookerId(booker1.getId(), pageable).getContent();

        List<Booking> expectedList = List.of(bookingInCurrentAndApprovedAndEndIn5Days,
                bookingInPastAndRejectedAndEndWas3DaysAgo);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByBookerIdAndCurrentState() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndCurrentState(booker1.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInCurrentAndApprovedAndEndIn5Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByBookerIdAndPastState() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndPastState(booker1.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInPastAndRejectedAndEndWas3DaysAgo,
                bookingInPastAndApprovedAndEndWas5DaysAgo);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByBookerIdAndFutureState() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndFutureState(booker1.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInFutureAndWaitingAndEndIn15Days,
                bookingInFutureAndApprovedAndEndIn7Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByBookerIdAndWaitingOrRejectedState_whenStatusIsRejected_thenReturnedCorrectList() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndWaitingOrRejectedState(booker1.getId(),
                Status.REJECTED, defaultPageable).getContent();
        List<Booking> expectedList = List.of(bookingInPastAndRejectedAndEndWas3DaysAgo);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByOwnerId_whenPageableIsSet_thenReturnedCorrectListWithCorrectSizeAndRightSort() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "end"));
        List<Booking> result = bookingRepository.findAllByOwnerId(owner.getId(), pageable).getContent();

        List<Booking> expectedList = List.of(bookingInFutureAndWaitingAndEndIn15Days,
                bookingFromBooker2InFutureAndStatusIsWaitingAndEndIn10Days, bookingInFutureAndApprovedAndEndIn7Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByOwnerIdAndCurrentState() {
        List<Booking> result = bookingRepository.findAllByOwnerIdAndCurrentState(owner.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInCurrentAndApprovedAndEndIn5Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByOwnerIdAndPastState() {
        List<Booking> result = bookingRepository.findAllByOwnerIdAndPastState(owner.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInPastAndRejectedAndEndWas3DaysAgo,
                bookingInPastAndApprovedAndEndWas5DaysAgo);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByOwnerIdAndFutureState() {
        List<Booking> result = bookingRepository.findAllByOwnerIdAndFutureState(owner.getId(), defaultPageable)
                .getContent();
        List<Booking> expectedList = List.of(bookingInFutureAndWaitingAndEndIn15Days,
                bookingFromBooker2InFutureAndStatusIsWaitingAndEndIn10Days, bookingInFutureAndApprovedAndEndIn7Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByOwnerIdAndWaitingOrRejectedState_whenStatusIsWaiting_thenReturnedCorrectList() {
        List<Booking> result = bookingRepository.findAllByOwnerIdAndWaitingOrRejectedState(owner.getId(),
                Status.WAITING, defaultPageable).getContent();
        List<Booking> expectedList = List.of(bookingInFutureAndWaitingAndEndIn15Days,
                bookingFromBooker2InFutureAndStatusIsWaitingAndEndIn10Days);

        assertEquals(result, expectedList);
    }

    @Test
    void findAllByItemsId() {
        List<Long> itemsId = List.of(item2.getId(), item5.getId());
        List<Booking> result = bookingRepository.findAllByItemsId(itemsId);

        List<Booking> expectedList = List.of(bookingInPastAndRejectedAndEndWas3DaysAgo,
                bookingInFutureAndWaitingAndEndIn15Days);

        assertEquals(result, expectedList);
    }
}