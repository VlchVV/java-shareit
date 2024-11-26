package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;

@Getter
@Setter
public class ItemDto {
    private Long id;
    @NotBlank
    @Size(max = 500)
    private String name;
    @NotBlank
    @Size(max = 2000)
    private String description;
    Long owner;
    @NotNull
    private Boolean available;
    private BookingOutputDto lastBooking;
    private BookingOutputDto nextBooking;
    private List<CommentDto> comments;
}