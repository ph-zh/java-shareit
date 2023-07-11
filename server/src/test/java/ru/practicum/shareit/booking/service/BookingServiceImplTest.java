package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    BookingMapper bookingMapper;

    private final Pageable defaultPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "end"));
    private Integer from = 0;
    private Integer size = 10;

    @Test
    void create_whenAllParamsAreValid_thenReturnedCorrectObjectAndInvokedSaveInDb() {
        long userId = 1L;
        long itemId = 1L;
        long ownerId = 2L;
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .itemId(itemId)
                .build();
        Item item = Item.builder()
                .owner(ownerId)
                .available(true)
                .build();
        User user = User.builder().build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Booking booking = Booking.builder().build();
        when(bookingMapper.toBooking(bookingCreationDto, item, user)).thenReturn(booking);

        BookingDto expectedBookingDto = BookingDto.builder().build();
        when(bookingMapper.toBookingDto(booking)).thenReturn(expectedBookingDto);

        BookingDto actualBookingDto = bookingService.create(userId, bookingCreationDto);

        assertEquals(expectedBookingDto, actualBookingDto);

        verify(bookingRepository).save(booking);
    }

    @Test
    void create_whenItemIsNotFound_thenReturnedNotFoundException() {
        long userId = 1L;
        long itemId = 1L;
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .itemId(itemId)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.create(userId, bookingCreationDto));

        assertEquals(exception.getMessage(), String.format("item with id: %d not found",
                bookingCreationDto.getItemId()));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenItemIsNotAvailable_thenReturnedBadRequestException() {
        long userId = 1L;
        long itemId = 1L;
        long ownerId = 2L;
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .itemId(itemId)
                .build();
        Item item = Item.builder()
                .owner(ownerId)
                .available(false)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                bookingService.create(userId, bookingCreationDto));

        assertEquals(exception.getMessage(), String.format("item with id: %d is currently unavailable", item.getId()));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenItemOwnerTryCreateBooking_thenReturnedNotFoundException() {
        long userId = 1L;
        long itemId = 1L;
        long ownerId = 1L;
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .itemId(itemId)
                .build();
        Item item = Item.builder()
                .owner(ownerId)
                .available(true)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.create(userId, bookingCreationDto));

        assertEquals(exception.getMessage(), "the owner cannot book his own item");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_whenParamApprovedIsTrue_thenReturnedCorrectObject() {
        long bookingId = 1L;
        boolean approved = true;
        long userId = 1L;
        long ownerId = 1L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        Booking booking = Booking.builder()
                .status(Status.WAITING)
                .item(item)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ItemDto itemDto = ItemDto.builder()
                .owner(ownerId)
                .build();

        BookingDto expectedBookingDto = BookingDto.builder()
                .status(Status.APPROVED)
                .item(itemDto)
                .build();

        when(bookingMapper.toBookingDto(booking)).thenReturn(expectedBookingDto);

        BookingDto actualBookingDto = bookingService.approve(bookingId, approved, userId);

        assertEquals(expectedBookingDto, actualBookingDto);
    }

    @Test
    void approve_whenParamApprovedIsFalse_thenReturnedCorrectObject() {
        long bookingId = 1L;
        boolean approved = false;
        long userId = 1L;
        long ownerId = 1L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        Booking booking = Booking.builder()
                .status(Status.WAITING)
                .item(item)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ItemDto itemDto = ItemDto.builder()
                .owner(ownerId)
                .build();

        BookingDto expectedBookingDto = BookingDto.builder()
                .status(Status.REJECTED)
                .item(itemDto)
                .build();

        when(bookingMapper.toBookingDto(booking)).thenReturn(expectedBookingDto);

        BookingDto actualBookingDto = bookingService.approve(bookingId, approved, userId);

        assertEquals(expectedBookingDto, actualBookingDto);
    }

    @Test
    void approve_whenUserIsNotBookingOwner_thenReturnedNotFoundException() {
        long bookingId = 1L;
        boolean approved = true;
        long userId = 2L;
        long ownerId = 1L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        Booking booking = Booking.builder()
                .status(Status.WAITING)
                .item(item)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.approve(bookingId, approved, userId));

        assertEquals(exception.getMessage(), String.format("this item is not owned by the user with id: %d", userId));
    }

    @Test
    void approve_whenBookingIsAlreadyApproved_thenBadRequestException() {
        long bookingId = 1L;
        boolean approved = true;
        long userId = 1L;
        long ownerId = 1L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        Booking booking = Booking.builder()
                .status(Status.APPROVED)
                .item(item)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                bookingService.approve(bookingId, approved, userId));

        assertEquals(exception.getMessage(), "booking is already approved");
    }

    @Test
    void getById_whenAllParamsAreValid_thenReturnedCorrectObject() {
        long bookingId = 1L;
        long userId = 1L;
        long ownerId = 1L;
        long bookerId = 2L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        User user = User.builder()
                .id(bookerId)
                .build();

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ItemDto itemDto = ItemDto.builder()
                .owner(ownerId)
                .build();

        UserDto userDto = UserDto.builder()
                .id(bookerId)
                .build();

        BookingDto expectedBookingDto = BookingDto.builder()
                .item(itemDto)
                .booker(userDto)
                .build();

        when(bookingMapper.toBookingDto(booking)).thenReturn(expectedBookingDto);

        BookingDto actualBookingDto = bookingService.getById(bookingId, userId);

        assertEquals(expectedBookingDto, actualBookingDto);
    }

    @Test
    void getById_whenUserIsNotOwnerOfBookingItemAndNotBooker_thenReturnedNotFoundException() {
        long bookingId = 1L;
        long userId = 3L;
        long ownerId = 1L;
        long bookerId = 2L;

        Item item = Item.builder()
                .owner(ownerId)
                .build();

        User user = User.builder()
                .id(bookerId)
                .build();

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getById(bookingId, userId));

        assertEquals(exception.getMessage(), "the booking cannot be viewed by a non-owner of the item" +
                " or a non-creator of the booking");
    }

    @Test
    void getAllByBooker_whenStateIsAllAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.ALL;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerId(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsCurrentAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.CURRENT;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndCurrentState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsPastAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.PAST;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndPastState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsFutureAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.FUTURE;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndFutureState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsWaitingAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.WAITING;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndWaitingOrRejectedState(userId, Status.WAITING, defaultPageable))
                .thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsRejectedAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.REJECTED;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndWaitingOrRejectedState(userId, Status.REJECTED, defaultPageable))
                .thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenStateIsAllAndPageableIsSet_thenReturnedCorrectListOfObjects() {
        State state = State.ALL;
        long userId = 1L;
        Integer from = 1;
        Integer size = 3;
        Pageable pageable = PageRequest.of(from / size, size, Sort.Direction.DESC, "end");

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerId(userId, pageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByBooker(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByBooker_whenUserIsNotFound_thenReturnedNotFoundException() {
        State state = State.ALL;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getAllByBooker(state, userId, null, null));

        assertEquals(exception.getMessage(), String.format("user with id: %d does not exist yet", userId));
    }

    @Test
    void getAllByOwner_whenStateIsAllAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.ALL;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsCurrentAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.CURRENT;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndCurrentState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsPastAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.PAST;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndPastState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsFutureAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.FUTURE;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndFutureState(userId, defaultPageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsWaitingAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.WAITING;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndWaitingOrRejectedState(userId, Status.WAITING, defaultPageable))
                .thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsRejectedAndPageableIsDefault_thenReturnedCorrectListOfObjects() {
        State state = State.REJECTED;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndWaitingOrRejectedState(userId, Status.REJECTED, defaultPageable))
                .thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenStateIsAllAndPageableIsSet_thenReturnedCorrectListOfObjects() {
        State state = State.ALL;
        long userId = 1L;
        Integer from = 1;
        Integer size = 3;
        Pageable pageable = PageRequest.of(from / size, size, Sort.Direction.DESC, "end");

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(userId, pageable)).thenReturn(Page.empty());

        List<BookingDto> expectedList = Collections.emptyList();
        List<BookingDto> actualList = bookingService.getAllByOwner(state, userId, from, size);

        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllByOwner_whenUserIsNotFound_thenReturnedNotFoundException() {
        State state = State.ALL;
        long userId = 1L;

        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getAllByOwner(state, userId, null, null));

        assertEquals(exception.getMessage(), String.format("user with id: %d does not exist yet", userId));
    }
}