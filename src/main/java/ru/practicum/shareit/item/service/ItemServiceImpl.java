package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ItemDto save(long userId, ItemDto itemDto) {
        userService.getById(userId);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userId);

        itemRepository.save(item);

        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDtoWithBookingsAndComments> getAllUserItems(long userId, Integer from, Integer size) {
        userService.getById(userId);

        List<Item> items;

        Pageable pageable = PageRequest.of(from / size, size);
        items = itemRepository.findAllByOwner(userId, pageable).getContent();


        return addBookingsAndCommentsForItems(items);
    }

    @Override
    @Transactional
    public @Valid ItemDto update(long itemId, long userId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item with id: %d not found", itemId)));

        if (userId != item.getOwner()) {
            throw new NotFoundException(String.format("item with id: %d " +
                    "does not belong to the user with id: %d", itemId, userId));
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDtoWithBookingsAndComments getById(long itemId, long userId) {
        userService.getById(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item with id: %d not found", itemId)));


        List<Item> list = List.of(item);
        ItemDtoWithBookingsAndComments itemDtoWithBookingsAndComments = addBookingsAndCommentsForItems(list).get(0);

        if (userId != item.getOwner()) {
            itemDtoWithBookingsAndComments.setLastBooking(null);
            itemDtoWithBookingsAndComments.setNextBooking(null);
        }

        return itemDtoWithBookingsAndComments;
    }

    @Override
    public List<ItemDto> search(long userId, String text, Integer from, Integer size) {
        userService.getById(userId);
        List<Item> items;

        Pageable pageable = PageRequest.of(from / size, size);
        items = itemRepository.search(text.toLowerCase(), pageable).getContent();

        return itemMapper.toListOfItemDto(items);
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentCreationDto commentCreationDto) {
        UserDto userDto = userService.getById(userId);
        List<Booking> pastBookings = bookingRepository.findAllByBookerIdAndPastState(userId,
                Pageable.unpaged()).getContent();
        if (pastBookings.isEmpty()) {
            throw new BadRequestException(String.format("Booker with id: %d has never booked items in the past",
                    userId));
        }
        if (pastBookings.stream()
                .map(b -> b.getItem().getId())
                .noneMatch(id -> id.equals(itemId))) {
            throw new BadRequestException(String.format("The booker with id: %d has not yet booked an item with id: %d" +
                    " or the booked period has not yet expired", userId, itemId));
        }
        Item item = pastBookings.stream()
                .map(Booking::getItem)
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow();

        Comment comment = commentMapper.toComment(commentCreationDto);
        comment.setItem(item);
        comment.setAuthor(userMapper.toUser(userDto));
        comment.setCreated(Instant.now());

        commentRepository.save(comment);

        CommentDto commentDto = commentMapper.toCommentDto(comment);
        commentDto.setAuthorName(userDto.getName());
        return commentDto;
    }

    private List<ItemDtoWithBookingsAndComments> addBookingsAndCommentsForItems(List<Item> items) {
        List<Long> itemsId = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<BookingDtoForItem> bookings = bookingRepository.findAllByItemsId(itemsId).stream()
                .map(bookingMapper::toBookingDtoForItem)
                .collect(Collectors.toList());

        List<Comment> comments = commentRepository.findAllByItemsId(itemsId);

        Set<ItemDtoWithBookingsAndComments> itemsWithBookings =
                new TreeSet<>(Comparator.comparing(ItemDtoWithBookingsAndComments::getId));
        for (Item item : items) {
            ItemDtoWithBookingsAndComments itemDtoWithBookings = itemMapper.toItemDtoWithBookingAndComments(item);
            Set<BookingDtoForItem> nextBookings = new TreeSet<>(Comparator.comparing(BookingDtoForItem::getStart));
            Set<BookingDtoForItem> lastBookings = new TreeSet<>(Comparator.comparing(BookingDtoForItem::getEnd)
                    .reversed());
            for (BookingDtoForItem booking : bookings) {
                LocalDateTime now = LocalDateTime.now();
                if (item.getId().equals(booking.getItem().getId())) {
                    if (booking.getStart().isAfter(now)) {
                        nextBookings.add(booking);
                    } else {
                        lastBookings.add(booking);
                    }
                }
            }
            itemDtoWithBookings.setNextBooking(nextBookings.stream().findFirst().orElse(null));
            itemDtoWithBookings.setLastBooking(lastBookings.stream().findFirst().orElse(null));
            itemsWithBookings.add(itemDtoWithBookings);

            itemDtoWithBookings.setComments(new ArrayList<>());
            for (Comment comment : comments) {
                if (comment.getItem().getId().equals(item.getId())) {
                    CommentDto commentDto = commentMapper.toCommentDto(comment);
                    commentDto.setAuthorName(comment.getAuthor().getName());
                    itemDtoWithBookings.getComments().add(commentDto);
                }
            }
        }
        return new ArrayList<>(itemsWithBookings);
    }
}
