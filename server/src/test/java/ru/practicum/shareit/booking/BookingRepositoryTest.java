package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DirtiesContext
    void findAllByBookerId() {
        User itemOwnerUser = new User(null, "user001@email.com", "UserName001");
        userRepository.save(itemOwnerUser);

        User itemBookerUser = new User(null, "user002@email.com", "UserName002");
        itemBookerUser = userRepository.save(itemBookerUser);

        Item item = new Item(null, "item", "test item description", true,
                itemOwnerUser, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(8),
                item, itemBookerUser, BookingStatus.WAITING);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerId(itemBookerUser.getId(), Pageable.ofSize(10));

        assertThat(bookings.getFirst().getId(), equalTo(booking.getId()));
        assertThat(bookings.size(), equalTo(1));
    }

    @Test
    @DirtiesContext
    void findAllByOwnerId() {
        User itemOwnerUser = new User(null, "user001@email.com", "UserName001");
        itemOwnerUser = userRepository.save(itemOwnerUser);

        User itemBookerUser = new User(null, "user002@email.com", "UserName002");
        userRepository.save(itemBookerUser);

        Item item = new Item(null, "item", "test item description", true,
                itemOwnerUser, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(8),
                item, itemBookerUser, BookingStatus.WAITING);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByOwnerId(itemOwnerUser.getId(), Pageable.ofSize(10));

        assertThat(bookings.getFirst().getId(), equalTo(booking.getId()));
        assertThat(bookings.size(), equalTo(1));
    }

    @Test
    @DirtiesContext
    void findAllByBookerIdAndStateCurrent() {
        User booker = new User(null, "booker@example.com", "Booker");
        userRepository.save(booker);

        User owner = new User(null, "owner@example.com", "Owner");
        userRepository.save(owner);

        Item item = new Item(null, "Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStateCurrent(booker.getId(), Pageable.ofSize(10));

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.getFirst().getId(), is(notNullValue()));
        assertThat(bookings.getFirst().getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    @DirtiesContext
    void findAllByBookerIdAndStatePast() {
        User booker = new User(null, "booker@example.com", "Booker");
        userRepository.save(booker);

        User owner = new User(null, "owner@example.com", "Owner");
        userRepository.save(owner);

        Item item = new Item(null, "Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatePast(booker.getId(), Pageable.ofSize(10));

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.getFirst().getId(), is(notNullValue()));
        assertThat(bookings.getFirst().getBooker().getId(), is(booker.getId()));
    }

    @Test
    @DirtiesContext
    void findAllByBookerIdAndStateFuture() {
        User booker = new User(null, "booker@example.com", "Booker");
        userRepository.save(booker);

        User owner = new User(null, "owner@example.com", "Owner");
        userRepository.save(owner);

        Item item = new Item(null, "Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStateFuture(booker.getId(), Pageable.ofSize(10));

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.getFirst().getId(), is(notNullValue()));
        assertThat(bookings.getFirst().getBooker().getId(), is(booker.getId()));
    }

    @Test
    @DirtiesContext
    void findFirstByItemIdAndStartLessThanEqualAndStatus() {
        User booker = new User(null, "booker@example.com", "Booker");
        userRepository.save(booker);

        User owner = new User(null, "owner@example.com", "Owner");
        userRepository.save(owner);

        Item item = new Item(null, "Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Optional<Booking> foundBooking = bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(item.getId(),
                LocalDateTime.now(), BookingStatus.APPROVED, Sort.by(Sort.Direction.DESC, "end"));

        assertThat(foundBooking.isPresent(), is(true));
        assertThat(foundBooking.get().getId(), is(booking.getId()));
    }

    @Test
    @DirtiesContext
    void existsByBookerIdAndItemIdAndEndBefore() {
        User booker = new User(null, "booker@example.com", "Booker");
        userRepository.save(booker);

        User owner = new User(null, "owner@example.com", "Owner");
        userRepository.save(owner);

        Item item = new Item(null, "Item", "Description", true, owner, null);
        itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(booker.getId(), item.getId(), LocalDateTime.now());

        assertThat(exists, equalTo(true));
    }
}