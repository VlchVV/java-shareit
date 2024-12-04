package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.data.domain.Sort.Direction.DESC;

@DataJpaTest
class ItemRequestRepositoryTest {

    private final User user = new User(null, "user", "user@mail.ru");
    private final User requester = new User(null, "user2", "user2@mail.ru");
    private final Item item = new Item(null, "item", "cool", true, user, null);
    private final Booking booking = new Booking(1L,
            LocalDateTime.now().minusDays(8),
            LocalDateTime.now().plusDays(4),
            item, requester, BookingStatus.WAITING);
    private final ItemRequest request = new ItemRequest(1L, "description", requester, LocalDateTime.now());
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(user);
        userRepository.save(requester);
        itemRepository.save(item);
        bookingRepository.save(booking);
        requestRepository.save(request);
    }

    @Test
    @DirtiesContext
    void findAllByRequesterId() {
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(2L, Sort.by(DESC, "created"));

        assertThat(requests.getFirst().getId(), equalTo(request.getId()));
        assertThat(requests.size(), equalTo(1));
    }
}
