package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase
@WebMvcTest(controllers = UserController.class)
class UserControllerIT {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String contentType = "application/json";

    @SneakyThrows
    @Test
    void save_whenUserDtoValid_thenReturnedStatusIsOkAndServiceInvoked() {
        UserDto userDto = UserDto.builder()
                .name("user")
                .email("email@email.ru")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        verify(userService).save(userDto);
    }

    @SneakyThrows
    @Test
    void save_whenUserDtoNotValid_thenReturnedBadRequestAndServiceNotInvoked() {
        UserDto userDto = UserDto.builder()
                .name("")
                .email("email@email.ru")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).save(userDto);
    }

    @SneakyThrows
    @Test
    void update_whenUserDtoIsValid_thenReturnedStatusIsOkAndCorrectResponse() {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .id(userId)
                .name("updateName")
                .email("update@email.ru")
                .build();

        when(userService.update(userId, userDto)).thenReturn(userDto);

        String result = mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(userDto));
    }

    @SneakyThrows
    @Test
    void update_whenUserDtoNotValid_thenReturnedBadRequest() {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("")
                .email("email.ru")
                .build();

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(contentType)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getById() {
        long userId = 1L;
        UserDto userDto = UserDto.builder().build();

        when(userService.getById(userId)).thenReturn(userDto);

        String result = mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(userDto));
    }

    @SneakyThrows
    @Test
    void getAll() {
        List<UserDto> list = List.of(new UserDto());
        when(userService.getAllUsers()).thenReturn(list);

        String result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(result, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Test
    void delete() {
        long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).delete(userId);
    }
}