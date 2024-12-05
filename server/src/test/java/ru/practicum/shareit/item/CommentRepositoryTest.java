package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DirtiesContext
    void findAllByItemId() {
        final User user = new User(null, "user", "user@mail.ru");
        final Item item = new Item(null, "item", "cool", true, user, null);
        final Comment comment = new Comment(null, "abc", item, user,
                LocalDateTime.of(2024, 12, 1, 23, 21, 8));

        userRepository.save(user);
        itemRepository.save(item);
        commentRepository.save(comment);

        List<Comment> comments = commentRepository.findAllByItemId(item.getId());

        assertThat(comments.getFirst().getId(), notNullValue());
        assertThat(comments.getFirst().getText(), equalTo(comment.getText()));
        assertThat(comments.size(), equalTo(1));
    }
}
