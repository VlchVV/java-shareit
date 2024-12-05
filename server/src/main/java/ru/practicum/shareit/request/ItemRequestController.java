package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto saveNewItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.saveNewItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsByRequester(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getItemRequestsByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestParam(defaultValue = "1") Integer from,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAllItemRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@PathVariable long requestId,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getItemRequestById(requestId, userId);
    }
}
