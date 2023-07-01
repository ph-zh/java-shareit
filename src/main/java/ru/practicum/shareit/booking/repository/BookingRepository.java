package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //by booker
    @Query("select b from Booking b where b.booker.id = ?1")
    Page<Booking> findAllByBookerId(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and current_timestamp between b.start and b.end")
    Page<Booking> findAllByBookerIdAndCurrentState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and b.end < current_timestamp")
    Page<Booking> findAllByBookerIdAndPastState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start > current_timestamp")
    Page<Booking> findAllByBookerIdAndFutureState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2")
    Page<Booking> findAllByBookerIdAndWaitingOrRejectedState(Long userId, Status status, Pageable pageable);

    //by owner
    @Query("select b from Booking b where b.item.owner = ?1")
    Page<Booking> findAllByOwnerId(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.item.owner = ?1 and current_timestamp between b.start and b.end")
    Page<Booking> findAllByOwnerIdAndCurrentState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.item.owner = ?1 and b.end < current_timestamp")
    Page<Booking> findAllByOwnerIdAndPastState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.item.owner = ?1 and b.start > current_timestamp")
    Page<Booking> findAllByOwnerIdAndFutureState(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.item.owner = ?1 and b.status = ?2")
    Page<Booking> findAllByOwnerIdAndWaitingOrRejectedState(Long userId, Status status, Pageable pageable);

    //by items
    @Query("select b from Booking b where b.item.id in ?1")
    List<Booking> findAllByItemsId(List<Long> itemsId);
}
