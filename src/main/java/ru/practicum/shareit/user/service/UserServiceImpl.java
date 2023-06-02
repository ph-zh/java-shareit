package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.valid.Marker;

import java.util.List;

@Validated
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toListOfUserDto(userRepository.findAll());
    }

    @Override
    public UserDto getById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id: %d does not exist yet", id)));

        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {

        User user = userRepository.save(userMapper.toUser(userDto));

        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public @Validated(Marker.OnCreate.class) UserDto update(long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id: %d does not exist yet", id)));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
