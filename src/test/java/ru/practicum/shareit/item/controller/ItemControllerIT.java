package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerIT {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";
    private static final String header = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void save_whenItemDtoIsValid_thenReturnedStatusIsOkAndCorrectResponse() {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();

        when(itemService.save(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
    }

    @SneakyThrows
    @Test
    void save_whenItemDtoIsNotValid_thenReturnedStatusBadRequest() {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("desc")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).save(userId, itemDto);
    }

    @SneakyThrows
    @Test
    void update_whenItemIdAndUserIdAreValid_thenReturnedStatusIsOkAndCorrectResponse() {
        long itemId = 1L;
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("desc")
                .available(true)
                .build();

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
    }

    @SneakyThrows
    @Test
    void getItemById_whenItemIdAndUserIdAreValid_thenReturnedStatusOkAndCorrectResponse() {
        long itemId = 1L;
        long userId = 1L;

        ItemDtoWithBookingsAndComments item = ItemDtoWithBookingsAndComments.builder().build();

        when(itemService.getById(anyLong(), anyLong())).thenReturn(item);

        String result = mockMvc.perform(get("/items/{itemId}", itemId).header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(item), result);
    }

    @SneakyThrows
    @Test
    void getItemById_whenItemIdIsNotValid_thenReturnedStatusBadRequest() {
        long itemId = 0L;
        long userId = 1L;

        mockMvc.perform(get("/items/{itemId}", itemId).header(header, userId))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getById(itemId, userId);
    }

    @SneakyThrows
    @Test
    void getAllUserItems_whenParamsAreMissing_thenReturnedStatusIsOk() {
        long userId = 1L;

        mockMvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk());

        verify(itemService).getAllUserItems(userId, 0, 10);
    }

    @SneakyThrows
    @Test
    void getAllUserItems_whenParamsAreNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        int from = -1;
        int size = 10;

        mockMvc.perform(get("/items")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getAllUserItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    void search_whenTextIsNotBlank_thenReturnedStatusIsOk() {
        long userId = 0L;
        String text = "text";

        mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk());

        verify(itemService).search(userId, text, 0, 10);
    }

    @SneakyThrows
    @Test
    void search_whenTextIsBlank_thenReturnedEmptyList() {
        long userId = 0L;
        String text = "";

        String result = mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, never()).search(userId, text, null, null);

        assertEquals(objectMapper.writeValueAsString(Collections.emptyList()), result);
    }

    @SneakyThrows
    @Test
    void search_whenTextIsNull_thenReturnedBadRequest() {
        long userId = 0L;
        String text = null;

        mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isInternalServerError());

        verify(itemService, never()).search(userId, text, null, null);
    }

    @SneakyThrows
    @Test
    void addComment_whenAllParamsAreValid_thenReturnedStatusIsOk() {
        long userId = 1L;
        long itemId = 1L;
        CommentCreationDto commentCreationDto = new CommentCreationDto();
        commentCreationDto.setText("text");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(commentCreationDto)))
                .andExpect(status().isOk());

        verify(itemService).addComment(userId, itemId, commentCreationDto);
    }
}