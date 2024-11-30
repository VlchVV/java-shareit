package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") Long itemId,
                           @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItem(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "1") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam String text) {
        return itemService.searchItems(from, size, text);
    }

    @GetMapping
    public List<ItemDto> getUsersItems(@RequestParam(defaultValue = "1") Integer from,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getUsersItems(from, size, userId);
    }

    @PostMapping
    public ItemDto saveNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @RequestBody ItemDto itemDto) {
        itemDto.setOwner(userId);
        return itemService.saveItem(itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable("id") Long itemId,
                              @RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        itemDto.setOwner(userId);
        return itemService.updateItem(itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable long itemId,
                                 @RequestBody CommentDto commentDto,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.saveNewComment(commentDto, itemId, userId);
    }
}
