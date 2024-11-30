package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND current_timestamp BETWEEN b.start AND b.end")
    List<Booking> findAllByBookerIdAndStateCurrent(long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND current_timestamp > b.end")
    List<Booking> findAllByBookerIdAndStatePast(long brokerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND current_timestamp < b.start")
    List<Booking> findAllByBookerIdAndStateFuture(long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus bookingStatus, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1")
    List<Booking> findAllByOwnerId(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND current_timestamp BETWEEN b.start AND b.end")
    List<Booking> findAllByOwnerIdAndStateCurrent(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND current_timestamp > b.end")
    List<Booking> findAllByOwnerIdAndStatePast(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND current_timestamp < b.start")
    List<Booking> findAllByOwnerIdAndStateFuture(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = ?1 " +
            "AND b.status = ?2")
    List<Booking> findAllByOwnerIdAndStatus(long ownerId, BookingStatus bookingStatus, Pageable pageable);

    Optional<Booking> findFirstByItemIdAndStartLessThanEqualAndStatus(long itemId, LocalDateTime localDateTime,
                                                                      BookingStatus bookingStatus, Sort end);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatus(long itemId, LocalDateTime localDateTime,
                                                              BookingStatus bookingStatus, Sort end);

    Boolean existsByBookerIdAndItemIdAndEndBefore(long bookerId, long itemId, LocalDateTime localDateTime);
}