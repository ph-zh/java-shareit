package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public @Valid BookingDto create(long userId, BookingCreationDto bookingCreationDto) {
        Item item = itemRepository.findById(bookingCreationDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("item with id: %d not found",
                        bookingCreationDto.getItemId())));
        if (!item.getAvailable()) {
            throw new BadRequestException(String.format("item with id: %d is currently unavailable", item.getId()));
        }
        if (item.getOwner().equals(userId)) {
            throw new NotFoundException("the owner cannot book his own item");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id: %d does not exist yet", userId)));
        Booking booking = bookingMapper.toBooking(bookingCreationDto, item, user);
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public @Valid BookingDto approve(long bookingId, boolean approved, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("booking with id: %d does not found", bookingId)));

        if (!booking.getItem().getOwner().equals(userId)) {
            throw new NotFoundException(String.format("this item is not owned by the user with id: %d", userId));
        }
        if (booking.getStatus().equals(Status.APPROVED) && approved) {
            throw new BadRequestException("booking is already approved");
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public @Valid BookingDto getById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("booking with id: %d does not found", bookingId)));

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner() != userId) {
            throw new NotFoundException("the booking cannot be viewed by a non-owner of the item" +
                    " or a non-creator of the booking");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(State state, long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("user with id: %d does not exist yet", userId));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "end");
        Pageable pageable = PageRequest.of(from / size, size, sort);

        return getAllByBookerAndStateWithPaginationTerms(state, userId, pageable);
    }

    @Override
    public List<BookingDto> getAllByOwner(State state, long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("user with id: %d does not exist yet", userId));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "end");
        Pageable pageable = PageRequest.of(from / size, size, sort);

        return getAllByOwnerAndStateWithPaginationTerms(state, userId, pageable);

    }

    private List<BookingDto> getAllByBookerAndStateWithPaginationTerms(State state, long userId, Pageable pageable) {
        List<BookingDto> result = Collections.emptyList();

        switch (state) {
            case ALL:
                result = bookingMapper.toListOfBookingDto(bookingRepository.findAllByBookerId(userId,
                        pageable).getContent());
                break;
            case CURRENT:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByBookerIdAndCurrentState(userId, pageable).getContent());
                break;
            case PAST:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByBookerIdAndPastState(userId, pageable).getContent());
                break;
            case FUTURE:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByBookerIdAndFutureState(userId, pageable).getContent());
                break;
            case WAITING:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByBookerIdAndWaitingOrRejectedState(userId, Status.WAITING, pageable).getContent());
                break;
            case REJECTED:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByBookerIdAndWaitingOrRejectedState(userId, Status.REJECTED, pageable).getContent());
                break;
        }

        return result;
    }

    private List<BookingDto> getAllByOwnerAndStateWithPaginationTerms(State state, long userId, Pageable pageable) {
        List<BookingDto> result = Collections.emptyList();

        switch (state) {
            case ALL:
                result = bookingMapper.toListOfBookingDto(bookingRepository.findAllByOwnerId(userId, pageable).getContent());
                break;
            case CURRENT:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByOwnerIdAndCurrentState(userId, pageable).getContent());
                break;
            case PAST:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByOwnerIdAndPastState(userId, pageable).getContent());
                break;
            case FUTURE:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByOwnerIdAndFutureState(userId, pageable).getContent());
                break;
            case WAITING:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByOwnerIdAndWaitingOrRejectedState(userId, Status.WAITING, pageable).getContent());
                break;
            case REJECTED:
                result = bookingMapper.toListOfBookingDto(bookingRepository
                        .findAllByOwnerIdAndWaitingOrRejectedState(userId, Status.REJECTED, pageable).getContent());
                break;
        }
        return result;
    }
}
