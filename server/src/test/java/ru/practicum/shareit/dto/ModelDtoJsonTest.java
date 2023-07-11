package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ModelDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> jsonBookingDto;
    @Autowired
    private JacksonTester<ItemDto> jsonItemDto;
    @Autowired
    private JacksonTester<UserDto> jsonUserDto;
    @Autowired
    private JacksonTester<ItemRequestDto> jsonItemRequestDto;

    private LocalDateTime start = LocalDateTime.of(2023, 10, 20, 12, 50);
    private LocalDateTime end = start.plusDays(5);
    private Instant instant = start.toInstant(ZoneOffset.UTC);

    private ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("item")
            .owner(1L)
            .description("desc")
            .available(true)
            .requestId(1L)
            .build();

    private UserDto userDto = UserDto.builder()
            .id(1L)
            .name("user")
            .email("user@email.ru")
            .build();

    private BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(start)
            .end(end)
            .item(itemDto)
            .booker(userDto)
            .status(Status.WAITING)
            .build();

    private ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("desc")
            .requestor(1L)
            .created(instant)
            .items(List.of(itemDto))
            .build();

    @SneakyThrows
    @Test
    void testItemDto() {
        JsonContent<ItemDto> result = jsonItemDto.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.owner").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }

    @SneakyThrows
    @Test
    void testUserDto() {
        JsonContent<UserDto> result = jsonUserDto.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("user");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@email.ru");
    }

    @SneakyThrows
    @Test
    void testBookingDto() {
        JsonContent<BookingDto> result = jsonBookingDto.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-10-20T12:50:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-10-25T12:50:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(Status.WAITING.toString());

        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.owner").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);

        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("user");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("user@email.ru");
    }

    @SneakyThrows
    @Test
    void testItemRequestDto() {
        JsonContent<ItemRequestDto> result = jsonItemRequestDto.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathNumberValue("$.requestor").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(instant.toString());

        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].owner").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description").isEqualTo("desc");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
    }
}
