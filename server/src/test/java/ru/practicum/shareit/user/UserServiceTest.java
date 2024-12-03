package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private final long id = 1L;
    private final UserDto userDto = new UserDto(id, "user@mail.ru", "User");
    private final User user = new User(id, "user@mail.ru", "User");
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findAll_whenNoUsers_thenReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserDto> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }


    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> targetUsers = userService.getAllUsers();

        Assertions.assertNotNull(targetUsers);
        assertEquals(1, targetUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_whenUserFound_thenReturnedUser() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto actualUser = userService.getUser(id);

        assertEquals(user, UserMapper.dtoToUser(actualUser));
    }

    @Test
    void getUserById_whenUserNotFound_thenExceptionThrown() {
        when((userRepository).findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUser(2L));
    }

    @Test
    void saveNewUser_whenUserNameValid_thenSavedUser() {
        when(userRepository.save(any())).thenReturn(user);

        UserDto actualUser = userService.saveUser(userDto);

        assertEquals(UserMapper.dtoToUser(userDto), UserMapper.dtoToUser(actualUser));
    }

    @Test
    void saveNewUser_whenEmailNotUnique_thenThrowsNotUniqueEmailException() {
        when(userRepository.save(any())).thenThrow(new ValidationException("Email already exists"));

        assertThrows(ValidationException.class, () -> {
            userService.saveUser(userDto);
        });
    }

    @Test
    void saveNewUser_whenUserEmailDuplicate_thenNotSavedUser() {
        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

        assertThrows(DataIntegrityViolationException.class, () -> userService.saveUser(userDto));
    }

    @Test
    void deleteUser() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testValidateUniqueEmail_EmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User(id, "user@mail.ru", "Some User")));

        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class, () -> {
            userService.saveUser(userDto);
        });

        assertEquals("Email user@mail.ru уже зарегистрирован у пользователя с id = 1!", thrown.getMessage());
    }

    @Test
    public void testValidateUniqueEmail_EmailDoesNotExist() {
        UserDto uniqueUserDto = new UserDto(1L, "newuser@example.com", "Unique User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        assertDoesNotThrow(() -> userService.saveUser(uniqueUserDto));
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userDto));
    }

    @Test
    void updateUser_whenUserEmailIsNotUnique_thenThrowsNotUniqueEmailException() {
        UserDto userDtoWithDuplicateEmail = new UserDto(2L, "dupl@example.com", "New Name");
        when(userRepository.findByEmail("dupl@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(id, "dupl@example.com", "Existing User")));

        assertThrows(ConditionsNotMetException.class, () -> userService.updateUser(userDtoWithDuplicateEmail));
    }

}
