package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Запрос всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::userToDto)
                .toList();
    }

    @Override
    public UserDto getUser(Long id) {
        log.debug(String.format("Поиск пользователя по id = %d.", id));
        return UserMapper.userToDto(userRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id = %d не найден!", id))));
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        log.debug("Начато создание пользователя", userDto);
        validateBeforeSave(userDto);
        final User user = UserMapper.dtoToUser(userDto);
        log.debug("Пользователь создан", user);
        return UserMapper.userToDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserUpdateDto userDto) {
        log.debug("Начато обновление пользователя", userDto);
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
        log.debug("Пользователь обновлен", currentUser);
        return UserMapper.userToDto(userRepository.save(UserMapper.dtoToUser(currentUser)));
    }

    @Override
    public void deleteUser(Long id) {
        log.debug(String.format("Начато удаление пользователя с id = %d", id));
        if (userRepository.findById(id).isEmpty()) {
            log.error(String.format("Пользователь с id = %d не найден!", id));
            throw new NotFoundException(String.format("Пользователь с id = %d не найден!", id));
        }
        userRepository.deleteById(id);
        log.debug(String.format("Пользователь с id = %d удален", id));
    }

    private void validateBeforeSave(UserDto userDto) throws ConditionsNotMetException {
        log.debug("Начата проверка перед созданием пользователя", userDto);
        final Optional<User> currentUser = userRepository.findByEmail(userDto.getEmail());
        if (currentUser.isPresent()) {
            log.error(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                    userDto.getEmail(), currentUser.get().getId()));
            throw new ConditionsNotMetException(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                    userDto.getEmail(), currentUser.get().getId()));
        }
        log.debug("Проверка перед созданием пользователя завершена", userDto);
    }

    private void validateBeforeUpdate(UserUpdateDto userDto) {
        log.debug("Начата проверка перед обновлением пользователя", userDto);
        final Long userId = userDto.getId();
        getUser(userId);
        final String Email = userDto.getEmail();
        if (Objects.nonNull(Email) && !Email.isBlank()) {
            final Optional<User> currentUserOptional = userRepository.findByEmail(userDto.getEmail());
            if (currentUserOptional.isPresent()) {
                User user = currentUserOptional.get();
                if (!user.getId().equals(userId)) {
                    log.error(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                            userDto.getEmail(), user.getId()));
                    throw new ConditionsNotMetException(String.format("Email %s уже зарегистрирован у пользователя с id = %d!",
                            userDto.getEmail(), user.getId()));
                }
            }
        }
        log.debug("Проверка перед обновлением пользователя завершена", userDto);
    }
}