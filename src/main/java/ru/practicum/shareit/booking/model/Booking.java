package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = { "id" })
public class Booking {
    private Long id;
    @NotNull
    private LocalDateTime start;
    @NonNull
    private LocalDateTime end;
    @NotNull
    private Long item;
    @NonNull
    private Long booker;
    private BookingStatus status;
}
