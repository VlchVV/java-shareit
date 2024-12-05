package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void search() {
        final User user = new User(null, "user", "user@example.com");
        final Item item = new Item(null, "item", "desc", true, user, null);
        userRepository.save(user);
        itemRepository.save(item);

        List<Item> items = itemRepository.findByText("i", Pageable.ofSize(10));

        assertThat(items.getFirst().getId(), equalTo(1L));
        assertThat(items.getFirst().getName(), equalTo(item.getName()));
        assertThat(items.size(), equalTo(1));
    }
}
