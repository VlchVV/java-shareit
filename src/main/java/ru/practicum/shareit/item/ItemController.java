package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
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
    public ItemDto getItem(@PathVariable("id") @Positive Long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItem(itemId, userId);
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
    public ItemDto updateItem(@PathVariable("id") @Positive Long itemId,
                                 @Valid @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                 @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        itemDto.setOwner(userId);
        return itemService.updateItem(itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable long itemId,
                                 @Valid @RequestBody CommentDto commentDto,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.saveNewComment(commentDto, itemId, userId);
    }
}
