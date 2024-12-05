package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable("id") @Positive Long itemId,
                                          @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(defaultValue = "1") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size,
                                              @RequestParam @NotBlank String text,
                                              @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return itemClient.searchItems(from, size, text, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersItems(@RequestParam(defaultValue = "1") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size,
                                                @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return itemClient.getUsersItems(from, size, userId);
    }

    @PostMapping
    public ResponseEntity<Object> saveNewItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                              @Valid @RequestBody ItemDto itemDto) {
        itemDto.setOwner(userId);
        return itemClient.saveItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@PathVariable("id") @Positive Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        itemDto.setOwner(userId);
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable @Positive long itemId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return itemClient.saveNewComment(commentDto, itemId, userId);
    }
}
