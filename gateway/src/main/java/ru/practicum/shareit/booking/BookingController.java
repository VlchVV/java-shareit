package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> saveNewBooking(@Valid @RequestBody BookingDto bookingDto,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingClient.saveNewBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@PathVariable long bookingId,
                                          @RequestParam(name = "approved") Boolean isApproved,
                                          @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingClient.approve(bookingId, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable long bookingId,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestParam(defaultValue = "1") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "10") @Positive Integer size,
                                                 @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                 @RequestHeader("X-Sharer-User-Id") long bookerId) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getAllByBooker(from, size, state, bookerId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestParam(defaultValue = "1") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size,
                                                @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                @RequestHeader("X-Sharer-User-Id") long ownerId) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getAllByOwner(from, size, state, ownerId);
    }
}
