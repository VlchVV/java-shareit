package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItem(Long id, Long userId);

    List<ItemDto> getUsersItems(Long userId);

    List<ItemDto> searchItems(String text);

    ItemDto saveItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    CommentDto saveNewComment(CommentDto commentDto, long itemId, long userId);
}
