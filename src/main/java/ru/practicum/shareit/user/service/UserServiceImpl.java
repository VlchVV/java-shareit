package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::userToDto)
                .toList();
    }

    @Override
    public UserDto getUser(Long id) {
        return UserMapper.userToDto(userRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id = %d не найден!", id))));
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        validateBeforeSave(userDto);
        final User user = UserMapper.dtoToUser(userDto);
        return UserMapper.userToDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        validateBeforeUpdate(userDto);
        final Long userId = userDto.getId();
        final UserDto currentUser = getUser(userId);
        final String email = userDto.getEmail();
        if (Objects.nonNull(email) && !email.isBlank()) {
            currentUser.setEmail(email);
        }
        final String name = userDto.getName();
        if (Objects.nonNull(name) && !name.isBlank()) {
            currentUser.setName(name);
        }
        return UserMapper.userToDto(userRepository.update(UserMapper.dtoToUser(currentUser)));
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.delete(id).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id = %d не найден!", id));
        }
    }

    private void validateBeforeSave(UserDto userDto) throws ConditionsNotMetException {
        final Optional<User> currentUser = userRepository.findByEmail(userDto.getEmail());
        if (currentUser.isPresent()) {
            throw new ConditionsNotMetException(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                    userDto.getEmail(), currentUser.get().getId()));
        }
    }

    private void validateBeforeUpdate(UserDto userDto) {
        final Long userId = userDto.getId();
        getUser(userId);
        final String Email = userDto.getEmail();
        if (Objects.nonNull(Email) && !Email.isBlank()) {
            final Optional<User> currentUserOptional = userRepository.findByEmail(userDto.getEmail());
            if (currentUserOptional.isPresent()) {
                User user = currentUserOptional.get();
                if (!user.getId().equals(userId)) {
                    throw new ConditionsNotMetException(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                            userDto.getEmail(), user.getId()));
                }
            }
        }
    }
}