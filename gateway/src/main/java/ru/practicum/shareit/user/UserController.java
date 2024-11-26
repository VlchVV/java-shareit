package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable("id") @Positive Long userId) {
        return userService.getUser(userId);
    }

    @PostMapping
    public UserDto saveNewUser(@Valid @RequestBody UserDto userDto) {
        return userService.saveUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable("id") @Positive Long userId, @Valid @RequestBody UserUpdateDto userDto) {
        if (Objects.isNull(userDto.getEmail()) && Objects.isNull(userDto.getName())) {
            throw new ConditionsNotMetException("Должно быть заполнено поле email либо name!");
        }
        userDto.setId(userId);
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") @Positive Long userId) {
        userService.deleteUser(userId);
    }
}
