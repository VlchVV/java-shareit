package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public Booking dtoToBooking(BookingDto bookingDto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

    public BookingOutputDto bookingToOutputDto(Booking booking) {
        BookingOutputDto bookingOutputDto = new BookingOutputDto();
        bookingOutputDto.setId(booking.getId());
        bookingOutputDto.setStart(booking.getStart());
        bookingOutputDto.setEnd(booking.getEnd());
        bookingOutputDto.setItem(ItemMapper.itemToDto(booking.getItem()));
        bookingOutputDto.setBooker(UserMapper.userToDto(booking.getBooker()));
        bookingOutputDto.setStatus(booking.getStatus());
        return bookingOutputDto;
    }
}
