package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto getItem(Long id) {
        log.debug(String.format("Поиск вещи по id = %d.", id));
        return ItemMapper.itemToDto(itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Вещь с id = %d не найдена", id))));
    }

    @Override
    public List<ItemDto> getUsersItems(Long userId) {
        log.debug(String.format("Поиск вещей по id пользователя = %d.", userId));
        userService.getUser(userId);
        return itemRepository.findByOwner(userId).stream()
                .map(ItemMapper::itemToDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.debug(String.format("Поиск вещей по тексту = %s.", text));
        if (text.isBlank()) {
            log.debug("Тестовая строка поиска пустая");
            return Collections.emptyList();
        }
        return itemRepository.findByText(text).stream()
                .map(ItemMapper::itemToDto)
                .toList();
    }

    @Override
    public ItemDto saveItem(ItemDto itemDto) {
        log.debug("Начато создание вещи", itemDto);
        validateBeforeSave(itemDto);
        final Item item = ItemMapper.dtoToItem(itemDto);
        log.debug("Вещь создана", item);
        return ItemMapper.itemToDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        log.debug("Начато обновление вещи", itemDto);
        validateBeforeUpdate(itemDto);
        final Item item = ItemMapper.dtoToItem(itemDto);
        log.debug("Вещь обновлена", item);
        return ItemMapper.itemToDto(itemRepository.update(item));
    }

    private void validateBeforeSave(ItemDto itemDto) {
        log.debug("Начата проверка перед созданием вещи", itemDto);
        userService.getUser(itemDto.getOwner());
        log.debug("Проверка перед созданием вещи успешна", itemDto);
    }

    private void validateBeforeUpdate(ItemDto itemDto) {
        log.debug("Начата проверка перед обновлением вещи", itemDto);
        final Long itemId = itemDto.getId();
        final ItemDto currentItem = getItem(itemId);
        final Long ownerId = itemDto.getOwner();
        userService.getUser(ownerId);
        if (!currentItem.getOwner().equals(ownerId)) {
            log.error(String.format("Владелец %d не совпадает с пользователем %d!",
                    currentItem.getOwner(), ownerId));
            throw new ConditionsNotMetException(String.format("Владелец %d не совпадает с пользователем %d!",
                    currentItem.getOwner(), ownerId));
        }
        log.debug("Проверка перед обновдением вещи успешна", itemDto);
    }
}
