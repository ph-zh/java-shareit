package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerIT {

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";
    private static final String header = "X-Sharer-User-Id";

    @SneakyThrows
    @Test
    void create_whenParamsAreValid_thenReturnedCorrectObject() {
        long userId = 1L;
        BookItemRequestDto bookingDto = new BookItemRequestDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10));

        ResponseEntity<Object> response = new ResponseEntity<>(bookingDto, HttpStatus.OK);
        when(bookingClient.create(userId, bookingDto)).thenReturn(response);

        String result = mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(bookingDto));

        verify(bookingClient).create(userId, bookingDto);
    }

    @SneakyThrows
    @Test
    void create_whenDtoIsNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        BookItemRequestDto bookingDto = new BookItemRequestDto(1L, LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10));

        mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(userId, bookingDto);
    }

    @SneakyThrows
    @Test
    void approve_whenParamsAreValid_thenReturnedStatusIsOkAndClientInvoked() {
        long bookingId = 1L;
        boolean approved = true;
        long userId = 1L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk());

        verify(bookingClient).approve(bookingId, approved, userId);
    }

    @SneakyThrows
    @Test
    void approve_whenBookingIdIsNotValid_thenReturnedBadRequest() {
        long bookingId = 0L;
        boolean approved = true;
        long userId = 1L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).approve(bookingId, approved, userId);
    }

    @SneakyThrows
    @Test
    void getById() {
        long userId = 1L;
        long bookingId = 1L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(header, userId))
                .andExpect(status().isOk());

        verify(bookingClient).getById(userId, bookingId);
    }

    @SneakyThrows
    @Test
    void getAllByBooker_whenParamsAreValid_thenReturnedStatusIsOkAndClientInvoked() {
        long userId = 1L;
        String state = "current";
        int from = 1;
        int size = 1;

        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBooker(userId, BookingState.CURRENT, from, size);
    }

    @SneakyThrows
    @Test
    void getAllByBooker_whenParamsNotSet_thenReturnedStatusIsOkAndClientInvokedWithDefaultValues() {
        long userId = 1L;

        mockMvc.perform(get("/bookings")
                        .header(header, userId))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBooker(userId, BookingState.ALL, 0, 10);
    }

    @SneakyThrows
    @Test
    void getAllByBooker_whenParamsNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        String state = "notValidState";

        String response = mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("state", state))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("{\"error\":\"Unknown state: notValidState\"}", response);

        verify(bookingClient, never()).getAllByBooker(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getAllByOwner() {
        long userId = 1L;
        String state = "current";
        int from = 1;
        int size = 1;

        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByOwner(userId, BookingState.CURRENT, from, size);
    }
}