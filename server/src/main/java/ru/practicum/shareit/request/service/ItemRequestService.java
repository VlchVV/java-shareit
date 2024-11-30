package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestDto> getItemRequestsByRequester(long userId);

    List<ItemRequestDto> getAllItemRequests(Integer from, Integer size, long userId);

    ItemRequestDto saveNewItemRequest(ItemRequestDto itemRequestDto, long userId);

    ItemRequestDto getItemRequestById(long requestId, long userId);
}
