package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutputDto saveNewBooking(@RequestBody BookingDto bookingDto,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.save(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutputDto approve(@PathVariable long bookingId, @RequestParam(name = "approved") Boolean isApproved,
                                    @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.approve(bookingId, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingOutputDto getBookingById(@PathVariable long bookingId,
                                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingOutputDto> getAllByBooker(@RequestParam(defaultValue = "1") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                 @RequestHeader("X-Sharer-User-Id") long bookerId) {
        return bookingService.getAllByBooker(from, size, state, bookerId);
    }

    @GetMapping("/owner")
    public List<BookingOutputDto> getAllByOwner(@RequestParam(defaultValue = "1") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                @RequestHeader("X-Sharer-User-Id") long ownerId) {
        return bookingService.getAllByOwner(from, size, state, ownerId);
    }
}
