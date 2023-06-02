package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //by booker
    @Query("select b from Booking b where b.booker.id = ?1")
    List<Booking> findAllByBookerId(Long userId, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 " +
            " and current_timestamp between b.start and b.end")
    List<Booking> findAllByBookerIdAndCurrentState(Long userId, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 " +
            " and b.end < current_timestamp")
    List<Booking> findAllByBookerIdAndPastState(Long userId, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 " +
            " and b.start > current_timestamp")
    List<Booking> findAllByBookerIdAndFutureState(Long userId, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2")
    List<Booking> findAllByBookerIdAndWaitingOrRejectedState(Long userId, Status status, Sort sort);

    //by owner
    @Query("select b from Booking b where b.item.owner = ?1")
    List<Booking> findAllByOwnerId(Long userId, Sort sort);

    @Query("select b from Booking b where b.item.owner = ?1 " +
            " and current_timestamp between b.start and b.end")
    List<Booking> findAllByOwnerIdAndCurrentState(Long userId, Sort sort);

    @Query("select b from Booking b where b.item.owner = ?1 " +
            " and b.end < current_timestamp")
    List<Booking> findAllByOwnerIdAndPastState(Long userId, Sort sort);

    @Query("select b from Booking b where b.item.owner = ?1 " +
            " and b.start > current_timestamp")
    List<Booking> findAllByOwnerIdAndFutureState(Long userId, Sort sort);

    @Query("select b from Booking b where b.item.owner = ?1 and b.status = ?2")
    List<Booking> findAllByOwnerIdAndWaitingOrRejectedState(Long userId, Status status, Sort sort);

    //by items
    @Query("select b from Booking b where b.item.id in ?1")
    List<Booking> findAllByItemsId(List<Long> itemsId);
}
