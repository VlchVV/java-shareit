package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto getItem(Long id) {
        return ItemMapper.itemToDto(itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Вещь с id = %d не найдена", id))));
    }

    @Override
    public List<ItemDto> getUsersItems(Long userId) {
        userService.getUser(userId);
        return itemRepository.findByOwner(userId).stream()
                .map(ItemMapper::itemToDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findByText(text).stream()
                .map(ItemMapper::itemToDto)
                .toList();
    }

    @Override
    public ItemDto saveItem(ItemDto itemDto) {
        validateBeforeSave(itemDto);
        final Item item = ItemMapper.dtoToItem(itemDto);
        return ItemMapper.itemToDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        validateBeforeUpdate(itemDto);
        final Item item = ItemMapper.dtoToItem(itemDto);
        return ItemMapper.itemToDto(itemRepository.update(item));
    }

    private void validateBeforeSave(ItemDto itemDto) {
        userService.getUser(itemDto.getOwner());
    }

    private void validateBeforeUpdate(ItemDto itemDto) {
        final Long itemId = itemDto.getId();
        final ItemDto currentItem = getItem(itemId);
        final Long ownerId = itemDto.getOwner();
        userService.getUser(ownerId);
        if (!currentItem.getOwner().equals(ownerId)) {
            throw new ConditionsNotMetException(String.format("Владелец %d не совпадает с пользователем %d!",
                    currentItem.getOwner(), ownerId));
        }
    }
}
