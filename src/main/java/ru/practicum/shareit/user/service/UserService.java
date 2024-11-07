package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUser(Long id);

    UserDto saveUser(UserDto user);

    UserDto updateUser(UserUpdateDto user);

    void deleteUser(Long id);
}
