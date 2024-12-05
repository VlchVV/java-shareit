package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private final long id = 1L;
    private final long notOwnerId = 2L;
    private final User user = new User(id, "User", "user@mail.ru");
    private final Item item = new Item(id, "item", "item description", true, user, null);
    private final Comment comment = new Comment(null, "comment", item, user,
            LocalDateTime.now());
    private final Booking booking = new Booking(id, null, null, item, user, BookingStatus.WAITING);
    private final Item anotherItem = new Item(id, "item2", "item description", true, user, null);
    private final User notOwner = new User(2L, "User2", "user2@mail.ru");
    private final ItemDto itemDto = new ItemDto(id, "item", "item description", null,
            true, null, null, null, null);
    private final ItemDto itemDtoOut = new ItemDto(id, "item", "item description", null,
            true, null, null, null, null);
    private final CommentDto commentDto = new CommentDto(id, "abc", "User",
            LocalDateTime.of(2023, 7, 1, 12, 12, 12));
    private final ItemRequest itemRequest = new ItemRequest();
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserServiceImpl userService;

    @Test
    void shouldSaveItem_whenUserExists() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.save(any())).thenReturn(item);

        itemDto.setOwner(id);

        ItemDto actualItemDto = itemService.saveItem(itemDto);

        Assertions.assertEquals(item.getId(), actualItemDto.getId());
        Assertions.assertNull(item.getItemRequest());
    }

    @Test
    void shouldNotSaveItem_whenUserDoesNotExist() {
        when((userRepository).findById(2L)).thenReturn(Optional.empty());
        itemDto.setOwner(2L);
        Assertions.assertThrows(NotFoundException.class, () -> itemService.saveItem(itemDto));
    }

    @Test
    void shouldNotUpdateItem_whenUserIsNotTheOwner() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        itemDto.setOwner(2L);

        Assertions.assertThrows(ConditionsNotMetException.class, () -> itemService.updateItem(itemDto));
    }

    @Test
    void shouldThrowException_whenItemDoesNotExist() {
        when((itemRepository).findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.getItem(2L, id));
    }

    @Test
    void shouldReturnItems_whenOwnerRequestsWithPaging() {
        when(itemRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(item));

        List<ItemDto> targetItems = itemService.getUsersItems(0, 10, id);

        Assertions.assertNotNull(targetItems);
        Assertions.assertEquals(1, targetItems.size());
        verify(itemRepository, times(1))
                .findByOwnerId(anyLong(), any());
    }

    @Test
    void shouldReturnItems_whenSearchTextIsProvided() {
        when(itemRepository.findByText(any(), any())).thenReturn(List.of(item));

        List<ItemDto> targetItems = itemService.searchItems(0, 10, "abc");

        Assertions.assertNotNull(targetItems);
        Assertions.assertEquals(1, targetItems.size());
        verify(itemRepository, times(1))
                .findByText(any(), any());
    }

    @Test
    void shouldReturnEmptyList_whenSearchTextIsBlank() {
        List<ItemDto> targetItems = itemService.searchItems(0, 10, "");

        Assertions.assertTrue(targetItems.isEmpty());
        verify(itemRepository, never()).findByText(any(), any());
    }

    @Test
    void shouldSaveComment_whenUserIsBooker() {
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenReturn(comment);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        CommentDto actualComment = itemService.saveNewComment(new CommentDto(id, "comment", "user", LocalDateTime.now()), id, id);
        Assertions.assertEquals(commentDto.getId(), id);

    }

    @Test
    void shouldThrowException_whenUserIsNotBooker() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () ->
                itemService.saveNewComment(new CommentDto(null, "comment", "user", LocalDateTime.now()), id, id));
    }

    @Test
    void shouldUpdateItemAvailability_whenUserIsTheOwner() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        itemDto.setOwner(id);

        itemDto.setAvailable(false);
        ItemDto actualItemDto = itemService.updateItem(itemDto);

        Assertions.assertFalse(actualItemDto.getAvailable());
    }

    @Test
    void shouldThrowException_whenGettingNonexistentItem() {
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.getItem(id, id));
    }

    @Test
    void shouldReturnEmptyList_whenOwnerHasNoItems() {
        when(itemRepository.findByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<ItemDto> targetItems = itemService.getUsersItems(0, 10, id);

        Assertions.assertTrue(targetItems.isEmpty());
    }

    @Test
    void shouldReturnMultipleItems_whenSearchTextMatchesMultipleItems() {
        when(itemRepository.findByText(any(), any())).thenReturn(List.of(item, anotherItem));

        List<ItemDto> targetItems = itemService.searchItems(0, 10, "abc");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void shouldThrowNotBookerException_whenCommentTextIsEmpty() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Assertions.assertThrows(ValidationException.class, () ->
                itemService.saveNewComment(new CommentDto(null, "", "user", LocalDateTime.now()), id, id));
    }

    @Test
    void shouldReturnItemWithNoComments() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(id)).thenReturn(Collections.emptyList());

        ItemDto actualItemDto = itemService.getItem(id, notOwnerId);

        Assertions.assertTrue(actualItemDto.getComments().isEmpty());
    }

    @Test
    void shouldUpdateItemAvailability_whenChangingAvailability() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        itemDto.setAvailable(true);
        itemDto.setOwner(id);
        ItemDto actualItemDto = itemService.updateItem(itemDto);

        Assertions.assertTrue(actualItemDto.getAvailable());
    }

    @Test
    void shouldReturnItemsFilteredByOwner() {
        when(itemRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(item, anotherItem));

        List<ItemDto> targetItems = itemService.getUsersItems(0, 10, id);

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void shouldThrowException_whenUserTriesToUpdateNonExistentItem() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        itemDto.setId(99L);
        Assertions.assertThrows(NotFoundException.class, () -> itemService.updateItem(itemDto));
    }

    @Test
    void shouldReturnEmptyList_whenOwnerHasNoItemsWithPaging() {
        when(userService.getUser(id)).thenReturn(UserMapper.userToDto(user));
        when(itemRepository.findByOwnerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<ItemDto> targetItems = itemService.getUsersItems(0, 10, id);

        Assertions.assertTrue(targetItems.isEmpty());
    }

    @Test
    void shouldReturnItems_whenSearchTextMatchesMultipleItemsWithDifferentCases() {
        when(itemRepository.findByText(any(), any())).thenReturn(List.of(item, anotherItem));

        List<ItemDto> targetItems = itemService.searchItems(0, 10, "ITEM");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void shouldThrowException_whenSavingCommentForNonExistentItem() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.saveNewComment(new CommentDto(null, "comment", "user", LocalDateTime.now()), id, id));
    }

    @Test
    void shouldThrowException_whenUserTriesToSaveCommentWithoutBooking() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () -> itemService.saveNewComment(new CommentDto(null, "comment", "user", LocalDateTime.now()), id, id));
    }

    @Test
    void shouldUpdateItem_whenUserIsTheOwnerAndFieldsAreNull() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        itemDto.setDescription("New description");
        itemDto.setAvailable(null);
        itemDto.setOwner(id);

        ItemDto actualItemDto = itemService.updateItem(itemDto);

        Assertions.assertEquals(item.getName(), actualItemDto.getName());
        Assertions.assertEquals(item.getDescription(), actualItemDto.getDescription());
        Assertions.assertEquals(item.getAvailable(), actualItemDto.getAvailable());
    }

    @Test
    void shouldReturnItem_whenItemIsAvailable() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));

        ItemDto actualItemDto = itemService.getItem(id, id);

        Assertions.assertTrue(actualItemDto.getAvailable());
    }

    @Test
    void shouldReturnItem_whenItemIsNotAvailable() {
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatus(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.findAllByItemId(id)).thenReturn(List.of(comment));

        item.setAvailable(false);

        ItemDto actualItemDto = itemService.getItem(id, id);

        Assertions.assertFalse(actualItemDto.getAvailable());
    }

    @Test
    void shouldReturnItems_whenSearchTextMatchesMultipleItemsWithDifferentCasesInsensitive() {
        when(itemRepository.findByText(any(), any())).thenReturn(List.of(item, anotherItem));

        List<ItemDto> targetItems = itemService.searchItems(0, 10, "ItEm");

        Assertions.assertEquals(2, targetItems.size());
    }

    @Test
    void shouldThrowException_whenSavingCommentForItemWithoutBooking() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any())).thenReturn(false);

        Assertions.assertThrows(ValidationException.class, () -> itemService.saveNewComment(new CommentDto(null, "comment", "user", LocalDateTime.now()), id, id));
    }

    @Test
    void shouldThrowException_whenUserTriesToSaveCommentForNonExistentItem() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.saveNewComment(new CommentDto(null, "comment", "user", LocalDateTime.now()), id, id));
    }


}
