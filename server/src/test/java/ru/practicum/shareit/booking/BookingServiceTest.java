package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private final User user = new User(1L, "User", "user@mail.ru");
    private final User booker = new User(2L, "user2", "user2@mail.ru");
    private final Item item = new Item(1L, "item", "cool", true, user, null);
    private final Booking booking = new Booking(1L, LocalDateTime.now().minusYears(2), LocalDateTime.now().minusYears(1), item, booker, BookingStatus.WAITING);
    private final BookingDto bookingDto = new BookingDto(LocalDateTime.now().minusYears(2), LocalDateTime.now().minusYears(1), 1L);
    private final BookingDto bookingDtoWrongItem = new BookingDto(LocalDateTime.now().minusYears(2), LocalDateTime.now().minusYears(1), 2L);
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void shouldThrowException_whenUser_DoesNotExist() {
        when((userRepository).findById(3L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.save(bookingDto, 3L));
    }

    @Test
    void shouldThrowException_whenItemDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when((itemRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.save(bookingDtoWrongItem, 2L));
    }

    @Test
    void shouldThrowExceptionItems_whenIsNotAvailable() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 2L));
    }

    @Test
    void shouldThrowException_whenBookerIsOwnerOfItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 1L));
    }

    @Test
    void shouldThrowException_whenOwnerAttemptsToBookOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 1L));
    }

    @Test
    void shouldApproveBooking_whenConditionsAreMet() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingOutputDto actualBooking = bookingService.approve(1L, true, 1L);

        assertEquals(BookingStatus.APPROVED, actualBooking.getStatus());
    }

    @Test
    void shouldThrowException_whenBookingDoesNotExist() {
        when((bookingRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.approve(2L, true, 1L));
    }

    @Test
    void shouldThrowException_whenItemIsAlreadyBooked() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        booking.setStatus(BookingStatus.APPROVED);

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 1L));
    }

    @Test
    void shouldThrowException_whenUserIsNeitherAuthorNorOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getBookingById(1L, 3L));
    }


    @Test
    void shouldReturnAllBookings_whenStateIsAll() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "ALL", 2L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnCurrentBookings_whenStateIsCurrent() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStateCurrent(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "CURRENT", 2L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnPastBookings_whenStateIsPast() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatePast(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "PAST", 2L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnFutureBookings_whenStateIsFuture() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStateFuture(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "FUTURE", 2L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnWaitingBookings_whenStateIsWaiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "WAITING", 2L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnCurrentBookingsForOwner_whenStateIsCurrent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStateCurrent(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "CURRENT", 1L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnFutureBookingsForOwner_whenStateIsFuture() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStateFuture(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "FUTURE", 1L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldReturnWaitingBookingsForOwner_whenStateIsWaiting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "WAITING", 1L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldThrowException_whenUserIsNotOwnerOfItem() {
        lenient().when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 2L));
    }

    @Test
    void shouldThrowException_whenBookingDoesNotExistInGetById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 2L));
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForBooker() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "ALL", 2L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "ALL", 1L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldThrowException_whenStateIsInvalidInGetAllByOwner() {
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getAllByOwner(0, 10, "INVALID_STATE", 1L));
    }


    @Test
    void shouldRejectBooking_whenConditionsAreMet() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingOutputDto actualBooking = bookingService.approve(1L, false, 1L);

        assertEquals(BookingStatus.REJECTED, actualBooking.getStatus());
    }

    @Test
    void shouldThrowException_whenGettingBookingWithInvalidId() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBookingById(2L, 1L));
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForOwnerInGetAllByOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "ALL", 1L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }


    @Test
    void shouldReturnEmptyList_whenNoBookingsForBookerInGetAllByBooker() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "ALL", 2L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldThrowException_whenItemIsNotAvailable() {
        User user = new User(2L, "Test User", "test@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 2L));
    }

    @Test
    void shouldThrowException_whenBookingIsNotPending() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 1L));
    }


    @Test
    void shouldThrowException_whenGettingBookingWithInvalidUser() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "Valid User", "valid@example.com"))); // Valid user
        lenient().when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "Another User", "another@example.com")));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.getBookingById(1L, 3L));
    }

    @Test
    void shouldReturnAllBookingsForOwner_whenStateIsRejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(List.of(booking));

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "REJECTED", 1L);

        assertEquals(actualBookings.size(), 1);
    }

    @Test
    void shouldThrowException_whenBookingIsNotFoundOnGetById() {
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBookingById(2L, 1L));
    }

    @Test
    void shouldThrowException_whenNoBookingsForOwnerAndStateIsWaiting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "WAITING", 1L);

        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForBookerWithStateRejected() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "REJECTED", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldThrowException_whenUserIsNotAvailable() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> bookingService.save(bookingDto, 2L));
    }

    @Test
    void shouldThrowException_whenBookingAlreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 1L));
    }

    @Test
    void shouldThrowException_whenGettingBookingWithNegativeId() {
        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBookingById(-1L, 1L));
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForBookerWithStateWaiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "WAITING", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForOwnerWithStateRejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStatus(anyLong(), any(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "REJECTED", 1L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldThrowException_whenItemIsNotAvailableForBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 2L));
    }

    @Test
    void shouldThrowException_whenGettingBookingByIdWithNegativeId() {
        Assertions.assertThrows(NotFoundException.class, () -> bookingService.getBookingById(-1L, 1L));
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForOwnerWithStateFuture() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwnerIdAndStateFuture(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByOwner(0, 10, "FUTURE", 1L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_whenNoBookingsForBookerWithStateFuture() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStateFuture(anyLong(), any())).thenReturn(Collections.emptyList());

        List<BookingOutputDto> actualBookings = bookingService.getAllByBooker(0, 10, "FUTURE", 2L);
        Assertions.assertTrue(actualBookings.isEmpty());
    }

    @Test
    void shouldThrowException_whenBookingIsNotPendingOnApprove() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 1L));
    }

    @Test
    void shouldThrowException_whenApprovingBookingWithInvalidUser() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Assertions.assertThrows(ValidationException.class, () -> bookingService.approve(1L, true, 2L));
    }

    @Test
    void shouldThrowException_whenItemIsNotAvailableDuringBookingPeriod() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        LocalDateTime now = LocalDateTime.now();
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(3));
        item.setAvailable(false);

        Assertions.assertThrows(ValidationException.class, () -> bookingService.save(bookingDto, 2L));
    }

    @Test
    void testGetAllByBooker_UnsupportedState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        Exception exception = assertThrows(ValidationException.class, () -> {
            bookingService.getAllByBooker(0, 10, "UNSUPPORTED_STATE", 1L);
        });
        assertEquals("Неизвестный тип состояния бронирования: UNSUPPORTED_STATE", exception.getMessage());
    }

    @Test
    void getAllByBooker_UnsupportedStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        String state = "UNSUPPORTED_STATUS";
        Exception exception = assertThrows(ValidationException.class, () -> {
            bookingService.getAllByBooker(0, 10, state, 1L);
        });
        assertEquals("Неизвестный тип состояния бронирования: UNSUPPORTED_STATUS", exception.getMessage());
    }

    @Test
    void getAllByBooker_All() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(booker.getId(), PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));


        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "ALL", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByBooker_Current() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStateCurrent(booker.getId(), PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));

        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "CURRENT", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByBooker_Past() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatePast(booker.getId(), PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));

        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "PAST", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByBooker_Future() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStateFuture(booker.getId(), PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));

        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "FUTURE", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByBooker_Waiting() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(booker.getId(), BookingStatus.WAITING, PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));

        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "WAITING", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByBooker_Rejected() {
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(booking);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatus(booker.getId(), BookingStatus.REJECTED, PageRequest.of(0, 10, Sort.by("start").descending()))).thenReturn(List.of(booking));

        List<BookingOutputDto> result = bookingService.getAllByBooker(0, 10, "REJECTED", booker.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
