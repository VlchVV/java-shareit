package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        User user = new User(null, "user@mail.ru", "user");
        assertThat(user.getId(), equalTo(null));
        userRepository.save(user);
        assertThat(user.getId(), notNullValue());
    }

    @Test
    void findByEmail() {
        User user = new User(null, "user2@mail.ru", "user");
        userRepository.save(user);
        Optional<User> userFound = userRepository.findByEmail("user2@mail.ru");
        assertThat(userFound.get().getId(), notNullValue());
        assertThat(userFound.get().getEmail(), equalTo("user2@mail.ru"));
    }
}
