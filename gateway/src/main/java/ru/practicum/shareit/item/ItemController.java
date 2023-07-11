package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> save(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestBody @Valid ItemRequestDto itemDto) {
        log.info("Creating item {} from user with id: {}", itemDto, userId);
        return itemClient.save(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable @Positive long itemId,
                                         @RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody ItemRequestDto itemDto) {
        log.info("Updating item {} with id: {}, from user with id: {}", itemDto, itemId, userId);
        return itemClient.update(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable @Positive long itemId,
                                              @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get item with id: {} from user with id: {}", itemId, userId);
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Get items from user with id: {}, from = {}, size = {}", userId, from, size);
        return itemClient.getAllUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Get items with name or description which contain text: {}, userId = {}, from = {}, size = {}",
                text, userId, from, size);
        if (text.isBlank()) {
            return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.OK);
        }
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable @Positive long itemId,
                                             @RequestBody @Valid CommentRequestDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
