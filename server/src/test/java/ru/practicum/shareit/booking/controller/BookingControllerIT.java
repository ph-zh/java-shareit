package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerIT {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";
    private static final String header = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void create_whenAllParamsAreValid_thenReturnedStatusIsOkAndCorrectResponse() {
        long userId = 1L;
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(10))
                .build();

        BookingDto bookingDto = BookingDto.builder().build();
        when(bookingService.create(userId, bookingCreationDto)).thenReturn(bookingDto);

        String result = mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(bookingCreationDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(bookingDto));
    }

    @SneakyThrows
    @Test
    void approve() {
        long bookingId = 1L;
        boolean approved = true;
        long userId = 1L;

        BookingDto bookingDto = BookingDto.builder().build();
        when(bookingService.approve(bookingId, approved, userId)).thenReturn(bookingDto);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(bookingDto));
    }

    @SneakyThrows
    @Test
    void getById() {
        long bookingId = 1L;
        long userId = 1L;

        BookingDto bookingDto = BookingDto.builder().build();
        when(bookingService.getById(bookingId, userId)).thenReturn(bookingDto);

        String result = mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(bookingDto));
    }

    @SneakyThrows
    @Test
    void getAllByBooker_whenAllParamsAreValid_thenReturnedStatusIsOkAndCorrectResponse() {
        long userId = 1L;
        int from = 1;
        int size = 10;

        List<BookingDto> list = List.of(BookingDto.builder().build());

        when(bookingService.getAllByBooker(any(State.class), anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(list);

        String result = mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void getAllByBooker_whenParamsFromAndSizeAreMissing_thenReturnedStatusIsOkAndCorrectResponse() {
        long userId = 1L;

        List<BookingDto> list = List.of(BookingDto.builder().build());

        when(bookingService.getAllByBooker(State.ALL, userId, 0, 10))
                .thenReturn(list);

        String result = mockMvc.perform(get("/bookings")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void getAllByOwner() {
        long userId = 1L;
        int from = 1;
        int size = 10;

        List<BookingDto> list = List.of(BookingDto.builder().build());

        when(bookingService.getAllByOwner(any(State.class), anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(list);

        String result = mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }
}