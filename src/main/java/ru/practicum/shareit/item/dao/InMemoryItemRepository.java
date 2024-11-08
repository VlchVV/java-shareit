package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.utils.BaseInMemoryRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryItemRepository extends BaseInMemoryRepository<Item> implements ItemRepository {
    @Override
    public Optional<Item> findById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<Item> findByOwner(Long userId) {
        return getFromStorage().stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    @Override
    public List<Item> findByText(String text) {
        final String searchText = text.toLowerCase();
        return getFromStorage().stream()
                .filter(item -> item.getAvailable() && (item.getName().toLowerCase().contains(searchText)
                        || item.getDescription().toLowerCase().contains(searchText)))
                .toList();
    }

    @Override
    public Item save(Item item) {
        final Long id = putInStorage(item);
        item.setId(id);
        return item;
    }

    @Override
    public Item update(Item item) {
        final Long itemId = item.getId();
        final Item currentItem = findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Вещь с id = %d не найдена!", itemId)));
        final String name = item.getName();
        if (Objects.nonNull(name) && !name.isBlank()) {
            currentItem.setName(name);
        }
        final String description = item.getDescription();
        if (Objects.nonNull(description) && !description.isBlank()) {
            currentItem.setDescription(description);
        }
        final Boolean available = item.getAvailable();
        if (Objects.nonNull(available)) {
            currentItem.setAvailable(available);
        }
        return currentItem;
    }
}
