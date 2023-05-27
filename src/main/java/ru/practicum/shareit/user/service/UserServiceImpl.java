package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.controller.exception.BadRequestException;
import ru.practicum.shareit.item.controller.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.valid.Marker;

import java.util.List;

@Validated
@RequiredArgsConstructor
@Service
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
    public UserDto save(UserDto userDto) {
        throwIfEmailDuplicate(userDto);

        User user = userRepository.save(userMapper.toUser(userDto));

        return userMapper.toUserDto(user);
    }

    @Override
    public @Validated(Marker.OnCreate.class) UserDto update(long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id: %d does not exist yet", id)));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !user.getEmail().equals(userDto.getEmail())) {
            throwIfEmailDuplicate(userDto);
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        return userMapper.toUserDto(user);
    }

    @Override
    public void delete(long id) {
        getById(id);
        userRepository.delete(id);
    }

    private void throwIfEmailDuplicate(UserDto userDto) {
        userRepository.findAll()
                .stream()
                .map(User::getEmail)
                .filter(email -> email.equals(userDto.getEmail()))
                .findFirst()
                .ifPresent(email -> {
                    throw new BadRequestException(String.format("user with email: %s already exist", email));
                });
    }

    public void checkExistUser(long id) {
        if (!userRepository.checkExistByUserId(id)) {
            throw new NotFoundException(String.format("user with id: %d does not exist yet", id));
        }
    }
}
