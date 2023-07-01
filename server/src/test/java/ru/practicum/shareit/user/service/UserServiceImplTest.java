package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Test
    void getAllUsers() {
        List<User> users = List.of(new User());
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> expectedList = userMapper.toListOfUserDto(users);
        List<UserDto> actualList = userService.getAllUsers();

        assertEquals(expectedList, actualList);
    }

    @Test
    void getById_whenUserIsExist_thenReturnedExpectedObject() {
        long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto expectedUserDto = userMapper.toUserDto(user);
        UserDto actualUserDto = userService.getById(userId);

        assertEquals(expectedUserDto, actualUserDto);
    }

    @Test
    void getById_whenUserIsNotExist_thenReturnedNotFoundException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getById(userId));

        assertEquals(exception.getMessage(), String.format("user with id: %d does not exist yet", userId));

        verify(userMapper, never()).toUserDto(any(User.class));
    }

    @Test
    void save() {
        UserDto userDtoForSave = new UserDto();

        User userForSave = new User();
        when(userMapper.toUser(any(UserDto.class))).thenReturn(userForSave);

        User user = userMapper.toUser(userDtoForSave);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto expectedUserDto = new UserDto();
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedUserDto);

        UserDto actualUserDto = userService.save(userDtoForSave);

        assertEquals(expectedUserDto, actualUserDto);
    }

    @Test
    void update() {
        long userId = 1L;
        UserDto userDtoForUpdate = UserDto.builder()
                .email("update@email.ru")
                .name("updateName")
                .build();

        User oldUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

        UserDto expectedUserDto = UserDto.builder()
                .email("update@email.ru")
                .name("updateName")
                .build();

        when(userMapper.toUserDto(oldUser)).thenReturn(expectedUserDto);

        UserDto actualUserDto = userService.update(userId, userDtoForUpdate);

        assertEquals(expectedUserDto, actualUserDto);
    }

    @Test
    void delete() {
        long userId = 1L;
        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }
}