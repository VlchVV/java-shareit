package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;

@Getter
@Setter
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Long owner;
    private Boolean available;
    private BookingOutputDto lastBooking;
    private BookingOutputDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;
}