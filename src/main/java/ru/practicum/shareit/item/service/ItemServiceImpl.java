package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto getItem(Long id, Long userId) {
        log.debug(String.format("Поиск вещи по id = %d.", id));
        return addBookingsAndComments(getItemById(id), userId);
    }

    @Override
    public List<ItemDto> getUsersItems(Long userId) {
        log.debug(String.format("Поиск вещей по id пользователя = %d.", userId));
        userService.getUser(userId);
        return itemRepository.findByOwnerId(userId).stream()
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
        final Item item = ItemMapper.dtoToItem(itemDto, getUserById(itemDto.getOwner()));
        log.debug("Вещь создана", item);
        return ItemMapper.itemToDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        log.debug("Начато обновление вещи", itemDto);

        final Long itemId = itemDto.getId();
        final Long ownerId = itemDto.getOwner();
        final String name = itemDto.getName();
        final String description = itemDto.getDescription();
        final Boolean available = itemDto.getAvailable();

        final Item currentItem = getItemById(itemId);

        userService.getUser(ownerId);
        if (!currentItem.getOwner().getId().equals(ownerId)) {
            log.error(String.format("Владелец %d не совпадает с пользователем %d!",
                    currentItem.getOwner().getId(), ownerId));
            throw new ConditionsNotMetException(String.format("Владелец %d не совпадает с пользователем %d!",
                    currentItem.getOwner().getId(), ownerId));
        }

        if (name != null && !name.isBlank()) {
            currentItem.setName(name);
        }
        if (description != null && !description.isBlank()) {
            currentItem.setDescription(description);
        }
        if (available != null) {
            currentItem.setAvailable(available);
        }

        log.debug("Вещь обновлена", currentItem);
        return ItemMapper.itemToDto(itemRepository.save(currentItem));
    }

    @Override
    public CommentDto saveNewComment(CommentDto commentDto, long itemId, long userId) {
        User user = getUserById(userId);
        Item item = getItemById(itemId);
        if (!bookingRepository.existsByBookerIdAndItemIdAndEndBefore(user.getId(), item.getId(), LocalDateTime.now())) {
            throw new ValidationException("Пользователь не может оставлять отзыв, т.к. не пользовался вещью");
        }
        Comment comment = commentRepository.save(CommentMapper.dtoToComment(commentDto, item, user));
        return CommentMapper.commentToDto(comment);
    }

    private void validateBeforeSave(ItemDto itemDto) {
        log.debug("Начата проверка перед созданием вещи", itemDto);
        userService.getUser(itemDto.getOwner());
        log.debug("Проверка перед созданием вещи успешна", itemDto);
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }

    private Item getItemById(Long id) {
        log.debug(String.format("Поиск вещи по id = %d.", id));
        return itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Вещь с id = %d не найдена", id)));
    }

    private ItemDto addBookingsAndComments(Item item, long userId) {
        ItemDto itemDto = ItemMapper.itemToDto(item);

        LocalDateTime thisMoment = LocalDateTime.now();
        if (itemDto.getOwner() == userId) {
            itemDto.setLastBooking(bookingRepository
                    .findFirstByItemIdAndStartLessThanEqualAndStatus(itemDto.getId(), thisMoment,
                            BookingStatus.APPROVED, Sort.by(DESC, "end"))
                    .map(BookingMapper::bookingToOutputDto)
                    .orElse(null));

            itemDto.setNextBooking(bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatus(itemDto.getId(), thisMoment,
                            BookingStatus.APPROVED, Sort.by(ASC, "end"))
                    .map(BookingMapper::bookingToOutputDto)
                    .orElse(null));
        }

        itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId())
                .stream()
                .map(CommentMapper::commentToDto)
                .collect(toList()));

        return itemDto;
    }
}
