package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                             @RequestBody @Valid BookingCreationDto bookingCreationDto) {
        return bookingService.create(userId, bookingCreationDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable @Positive long bookingId,
                              @RequestParam boolean approved,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable @Positive long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestParam(defaultValue = "ALL") String state,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getAllByBooker(throwIfStateNotValid(state), userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestParam(defaultValue = "ALL") String state,
                                          @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getAllByOwner(throwIfStateNotValid(state), userId);
    }

    private State throwIfStateNotValid(String state) {
        try {
            return State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
