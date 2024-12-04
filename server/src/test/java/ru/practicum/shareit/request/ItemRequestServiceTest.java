package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    private final User requester = new User(2L, "user2", "user2@mail.ru");
    private final User user = new User(1L, "User", "user@mail.ru");
    private final ItemRequest request = new ItemRequest(1L, "description", requester, LocalDateTime.now());
    private final Item item = new Item(1L, "item", "cool", true, user, request);
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void shouldReturnSavedRequest_whenNewRequestIsCreated() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(requestRepository.save(any())).thenReturn(request);

        final ItemRequestDto actualRequest = itemRequestService.saveNewItemRequest(
                new ItemRequestDto(null, "description", null, null, null), 2L);

        Assertions.assertEquals(actualRequest.getId(), 1L);
    }

    @Test
    void shouldReturnSavedRequests_whenRequesterIsFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequesterId(anyLong(), any())).thenReturn(List.of(request));
        when(itemRepository.findAllByItemRequestId(1L)).thenReturn(List.of(item));
        final ItemRequestDto requestDtoOut = ItemRequestMapper.itemRequestToDto(request);
        requestDtoOut.setItems(List.of(ItemMapper.itemToDto(item)));

        List<ItemRequestDto> actualRequests = itemRequestService.getItemRequestsByRequester(2L);

        Assertions.assertEquals(actualRequests.size(), 1);
    }

    @Test
    void shouldThrowException_whenRequesterIsNotFound() {
        when((userRepository).findById(3L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestsByRequester(3L));
    }

    @Test
    void shouldReturnRequestById_whenUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.findAllByItemRequestId(1L)).thenReturn(List.of(item));
        final ItemRequestDto requestDto = ItemRequestMapper.itemRequestToDto(request);
        requestDto.setItems(List.of(ItemMapper.itemToDto(item)));

        ItemRequestDto actualRequest = itemRequestService.getItemRequestById(1L, 1L);

        Assertions.assertEquals(actualRequest.getId(), 1L);
    }

    @Test
    void shouldThrowException_whenUserNotFoundOnSaveRequest() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                itemRequestService.saveNewItemRequest(new ItemRequestDto(null, "description", null, null, null), 2L));
    }

    @Test
    void shouldReturnEmptyList_whenNoRequestsFoundForRequester() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(requestRepository.findAllByRequesterId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<ItemRequestDto> actualRequests = itemRequestService.getItemRequestsByRequester(2L);

        Assertions.assertTrue(actualRequests.isEmpty());
    }

    @Test
    void shouldThrowException_whenUserNotFoundOnGetAllRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllItemRequests(0, 10, 1L));
    }

    @Test
    void shouldThrowException_whenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestById(1L, 1L));
    }

    @Test
    void shouldReturnEmptyList_whenNoRequestsFoundInPagination() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterIdIsNot(eq(userId), any(Pageable.class))).thenReturn(Collections.emptyList());

        List<ItemRequestDto> actualRequests = itemRequestService.getAllItemRequests(0, 10, userId);

        Assertions.assertTrue(actualRequests.isEmpty(), "Expected an empty list when no requests are found");
    }
}
