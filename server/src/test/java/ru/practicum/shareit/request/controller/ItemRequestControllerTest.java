package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";
    private static final String header = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void add_whenAllParamsAreValid_thenReturnedStatsIsOkAndCorrectResponse() {
        long userId = 1L;
        ItemRequestCreationDto itemRequestCreationDto = new ItemRequestCreationDto();
        itemRequestCreationDto.setDescription("desc");

        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();

        when(itemRequestService.add(userId, itemRequestCreationDto)).thenReturn(itemRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(itemRequestCreationDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(itemRequestDto));
    }

    @SneakyThrows
    @Test
    void getAllByOwner() {
        long userId = 1L;

        List<ItemRequestDto> list = Collections.emptyList();

        when(itemRequestService.getAllByOwner(userId)).thenReturn(list);

        String result = mockMvc.perform(get("/requests")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void getAll() {
        long userId = 1L;
        Integer from = null;
        Integer size = null;

        List<ItemRequestDto> list = Collections.emptyList();

        when(itemRequestService.getAll(userId, from, size)).thenReturn(list);

        String result = mockMvc.perform(get("/requests/all")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void getById() {
        long userId = 1L;
        long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();

        when(itemRequestService.getById(userId, requestId)).thenReturn(itemRequestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(itemRequestDto));
    }
}