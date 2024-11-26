package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "bookings")
@EqualsAndHashCode(of = { "id" })
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_dt", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_dt", nullable = false)
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
