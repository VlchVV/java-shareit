package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;

import java.util.List;

public interface BookingService {
    BookingOutputDto save(BookingDto bookingDto, long userId);

    BookingOutputDto approve(long bookingId, Boolean isApproved, long userId);

    BookingOutputDto getBookingById(long bookingId, long userId);

    List<BookingOutputDto> getAllByBooker(Integer from, Integer size, String state, long bookerId);

    List<BookingOutputDto> getAllByOwner(Integer from, Integer size, String state, long ownerId);
}
