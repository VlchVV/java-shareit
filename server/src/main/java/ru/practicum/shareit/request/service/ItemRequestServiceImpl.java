package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getItemRequestsByRequester(long userId) {
        log.info("Получение всех запросов по просителю с идентификатором {}", userId);
        getUser(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(userId, Sort.by(DESC, "created"));
        return addItems(requests);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllItemRequests(Integer from, Integer size, long userId) {
        log.info("Запрос всех запросов вещей");
        getUser(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdIsNot(userId, pageable);
        return addItems(requests);
    }

    @Override
    public ItemRequestDto saveNewItemRequest(ItemRequestDto itemRequestDto, long userId) {
        log.debug("Начато создание запроса вещи", itemRequestDto);
        User requester = getUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.dtoToItemRequest(itemRequestDto, requester);
        itemRequest.setCreated(LocalDateTime.now());
        log.debug("Запрос вещи создан", itemRequestDto);
        return ItemRequestMapper.itemRequestToDto(requestRepository.save(itemRequest));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getItemRequestById(long requestId, long userId) {
        log.info(String.format("Поиск запроса вещи по id = %d.", requestId));
        getUser(userId);
        ItemRequestDto itemRequestDto = ItemRequestMapper.itemRequestToDto(requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Запрос вещи с id = %d не найдена", requestId))));
        itemRequestDto.setItems(itemRepository.findAllByItemRequestId(requestId).stream()
                .map(ItemMapper::itemToDto).collect(Collectors.toList()));
        return itemRequestDto;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id = %d не найдена", userId)));
    }

    private List<ItemRequestDto> addItems(List<ItemRequest> requests) {
        final List<ItemRequestDto> requestsOut = new ArrayList<>();
        for (ItemRequest request : requests) {
            ItemRequestDto itemRequestDto = ItemRequestMapper.itemRequestToDto(request);
            List<ItemDto> items = itemRepository.findAllByItemRequestId(request.getId()).stream()
                    .map(ItemMapper::itemToDto).collect(Collectors.toList());
            itemRequestDto.setItems(items);
            requestsOut.add(itemRequestDto);
        }
        return requestsOut;
    }
}