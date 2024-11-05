package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = { "id" })
public class Item {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private Boolean available;
    @NotNull
    private Long owner;
    private Long request;
}