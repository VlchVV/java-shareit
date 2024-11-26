package ru.practicum.shareit.request.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = { "id" })
public class ItemRequest {
    private Long id;
    private String description;
    @NotNull
    private Long requestor;
    private LocalDateTime created;
}
