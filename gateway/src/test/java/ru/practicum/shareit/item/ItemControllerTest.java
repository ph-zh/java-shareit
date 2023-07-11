package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";
    private static final String header = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void save_whenParamAreValid_thenReturnedCorrectObject() {
        long userId = 1L;
        ItemRequestDto itemDto = new ItemRequestDto(1L, "name", "desc", userId, true, 1L);

        ResponseEntity<Object> response = new ResponseEntity<>(itemDto, HttpStatus.OK);
        when(itemClient.save(userId, itemDto)).thenReturn(response);

        String result = mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(itemDto));

        verify(itemClient).save(userId, itemDto);
    }

    @SneakyThrows
    @Test
    void save_whenDtoIsNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        ItemRequestDto itemDto = new ItemRequestDto(1L, null, "desc", userId, true, 1L);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).save(userId, itemDto);
    }

    @SneakyThrows
    @Test
    void update_whenParamAreValid_thenReturnedCorrectObject() {
        long itemId = 1L;
        long userId = 1L;
        ItemRequestDto itemDto = new ItemRequestDto(1L, "name", "desc", userId, true, 1L);

        ResponseEntity<Object> response = new ResponseEntity<>(itemDto, HttpStatus.OK);
        when(itemClient.update(itemId, userId, itemDto)).thenReturn(response);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(itemDto));

        verify(itemClient).update(itemId, userId, itemDto);
    }

    @SneakyThrows
    @Test
    void getItemById() {
        long userId = 1L;
        long itemId = 1L;

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(header, userId))
                .andExpect(status().isOk());

        verify(itemClient).getById(itemId, userId);
    }

    @SneakyThrows
    @Test
    void getAllUserItems() {
        long userId = 1L;

        mockMvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk());

        verify(itemClient).getAllUserItems(userId, 0, 10);
    }

    @SneakyThrows
    @Test
    void search_whenParamsAreValid_thenReturnedCorrectObject() {
        long userId = 1L;
        String text = "text";
        ItemRequestDto itemDto = new ItemRequestDto(1L, null, "desc", userId, true, 1L);
        List<ItemRequestDto> list = List.of(itemDto);
        ResponseEntity<Object> response = new ResponseEntity<>(list, HttpStatus.OK);

        when(itemClient.search(userId, text, 0, 10)).thenReturn(response);

        String result = mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void search_whenParamTextIsBlank_thenReturnedEmptyList() {
        long userId = 1L;
        String text = "";
        List<ItemRequestDto> list = List.of();
        ResponseEntity<Object> response = new ResponseEntity<>(list, HttpStatus.OK);

        when(itemClient.search(userId, text, 0, 10)).thenReturn(response);

        String result = mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void addComment_whenParamsAreValid_thenReturnedStatusIsOk() {
        long userId = 1L;
        long itemId = 1L;
        CommentRequestDto commentDto = new CommentRequestDto("comment");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(userId, itemId, commentDto);
    }

    @SneakyThrows
    @Test
    void addComment_whenDtoIsNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        long itemId = 1L;
        CommentRequestDto commentDto = new CommentRequestDto("");

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(userId, itemId, commentDto);
    }
}