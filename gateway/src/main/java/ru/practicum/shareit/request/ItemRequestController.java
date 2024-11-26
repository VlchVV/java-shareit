package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> saveNewRequest(@Valid @RequestBody RequestDto requestDto,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.saveRequest(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestParam(defaultValue = "1") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "10") @Positive Integer size,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable long requestId,
                                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getRequestById(requestId, userId);
    }
}
