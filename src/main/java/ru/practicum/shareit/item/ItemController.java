package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") @Positive Long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam @NotNull String text) {
        return itemService.searchItems(text);
    }

    @GetMapping
    public List<ItemDto> getUsersItems(@Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.getUsersItems(userId);
    }

    @PostMapping
    public ItemDto saveNewItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                               @Valid @RequestBody ItemDto itemDto) {
        itemDto.setOwner(userId);
        return itemService.saveItem(itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateNewItem(@PathVariable("id") @Positive Long itemId,
                                 @Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                 @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        itemDto.setOwner(userId);
        return itemService.updateItem(itemDto);
    }
}
