package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingOutputDto save(BookingDto bookingDto, long userId) {
        User booker = getUser(userId);
        Item item = getItem(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new ValidationException("Нельзя забронировать свою вещь");
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования должна быть до даты возврата");
        }
        Booking booking = bookingRepository.save(BookingMapper.dtoToBooking(bookingDto, booker, item));
        log.info(String.format("Бронирование %d создано", booking.getId()));
        return BookingMapper.bookingToOutputDto(booking);
    }

    @Override
    public BookingOutputDto approve(long bookingId, Boolean isApproved, long userId) {
        Booking booking = getById(bookingId);

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже было подстверждено");
        }
        if (booking.getItem().getOwner().getId() != userId) {
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info(String.format("Бронирование %d обновлено", booking.getId()));
        return BookingMapper.bookingToOutputDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingOutputDto getBookingById(long bookingId, long userId) {
        Booking booking = getById(bookingId);
        User booker = booking.getBooker();
        User owner = getUser(booking.getItem().getOwner().getId());
        if (booker.getId() != userId && owner.getId() != userId) {
            throw new ValidationException(String.format("Пользователь %d не может просматривать бронирование %d",
                    userId, bookingId));
        }
        return BookingMapper.bookingToOutputDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutputDto> getAllByBooker(Integer from, Integer size, String state, long bookerId) {
        User booker = getUser(bookerId);
        List<Booking> bookings;
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Неизвестный тип состояния бронирования: %s", state));
        }
        bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerId(booker.getId(), pageable);
            case CURRENT -> bookingRepository.findAllByBookerIdAndStateCurrent(booker.getId(), pageable);
            case PAST -> bookingRepository.findAllByBookerIdAndStatePast(booker.getId(), pageable);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStateFuture(booker.getId(), pageable);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                    BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                    BookingStatus.REJECTED, pageable);
        };
        return bookings.stream().map(BookingMapper::bookingToOutputDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingOutputDto> getAllByOwner(Integer from, Integer size, String state, long ownerId) {
        User owner = getUser(ownerId);
        List<Booking> bookings;
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Неизвестный тип состояния бронирования: %s", state));
        }
        bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByOwnerId(owner.getId(), pageable);
            case CURRENT -> bookingRepository.findAllByOwnerIdAndStateCurrent(owner.getId(), pageable);
            case PAST -> bookingRepository.findAllByOwnerIdAndStatePast(owner.getId(), pageable);
            case FUTURE -> bookingRepository.findAllByOwnerIdAndStateFuture(owner.getId(), pageable);
            case WAITING -> bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                    BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                    BookingStatus.REJECTED, pageable);
        };
        return bookings.stream().map(BookingMapper::bookingToOutputDto).collect(Collectors.toList());
    }

    public Booking getById(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException(String.format("Бронирование %d не найдено", bookingId)));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь %d не найден", userId)));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Вещь %d не найдена", itemId)));
    }
}
