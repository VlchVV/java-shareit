package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @Future
    @NotNull
    private LocalDateTime end;
    @NotNull
    private Long itemId;
}
